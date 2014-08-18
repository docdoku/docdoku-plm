/*global App,APP_CONFIG,worker*/
'use strict';
define(['dmu/LoaderManager','lib/async'],
function (LoaderManager, async) {

    /**
     *  This class handles instances management.
     *
     *  Dialog to sceneManager  : add and remove meshes
     *  Dialog from sceneManager  : init and update camera and frustum
     *
     *  Dialog with worker :
     *   - insert and remove instances from tree,
     *   - update frustum and camera
     *
     *  Dialog from worker
     *   - show/hide instances and update instances quality
     *
     * */
    var InstancesManager = function () {
        var _this = this;

        this.xhrQueue=null;
        this.loadQueue=null;
        this.aborted = 0;
        this.alreadySameQuality = 0;
        this.xhrsDone = 0;

        var instancesIndexed={};
        var loadCache={};
        var loadedInstances = [];                                                                                       // Store all loaded geometries and materials
        var loaderManager = new LoaderManager({progressBar: true});
        var loaderIndicator = $('#product_title').find('img.loader');
        var timer = null;
        var evalRunning = false;

        var workerMessages = {
            stats: function (stats) {
                _this.workerStats = stats;
            },
            directives: function (directives) {
                _this.aborted+=_this.xhrQueue.tasks.length;
                _this.xhrQueue.kill();

                _(directives).each(function(directive){
                    var instance = _this.getInstance(directive.id);
                    if(directive.nowait && directive.quality === undefined){
                        App.sceneManager.removeMeshById(directive.id);
                        if(loadCache[instance.partIterationId+'-'+instance.qualityLoaded]){
                            if( loadCache[instance.partIterationId+'-'+instance.qualityLoaded].count === 1){
                                loadCache[instance.partIterationId+'-'+instance.qualityLoaded].geometry.dispose();
                                loadCache[instance.partIterationId+'-'+instance.qualityLoaded].material.dispose();
                                loadCache[instance.partIterationId+'-'+instance.qualityLoaded] = null;
                                delete loadCache[instance.partIterationId+'-'+instance.qualityLoaded];
                            }else{
                                loadCache[instance.partIterationId+'-'+instance.qualityLoaded].count--;
                            }
                        }
                        instance.qualityLoaded = undefined;
                        worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                    }else{
                        instance.directiveQuality = directive.quality;
                        _this.xhrQueue.push(directive);
                    }
                });


                setTimeout(function(){
                    evalRunning = false;
                },500);
            }
        };

        var worker = new Worker('/js/product-structure/workers/InstancesWorker.js');

        worker.addEventListener('message', function (message) {
            if (typeof  workerMessages[message.data.fn] === 'function') {
                workerMessages[message.data.fn](message.data.obj);
            } else {
                if(App.debug){
                    console.log('[InstancesManager] Unrecognized command  : ');
                    console.log(message.data);
                }
            }
        }, false);

        /**
         * Load process : xhr + store geometry and materials in array
         */
        function loadProcess(directive, callback){
            var instance = _this.getInstance(directive.id);
            if(!instance){
                setTimeout(callback,0);
                return;
            }

            if (directive.quality === undefined) {

                // don't unload edited meshes
                if (App.sceneManager.editedMeshes.indexOf(instance.id) !== -1) {
                    _this.aborted++;
                    worker.postMessage({fn:'setQuality',obj:{id:instance.id,quality:instance.qualityLoaded}});
                    setTimeout(callback,0);
                    return;
                }

                App.sceneManager.removeMeshById(instance.id);
                if(loadCache[instance.partIterationId+'-'+instance.qualityLoaded]){
                    if( loadCache[instance.partIterationId+'-'+instance.qualityLoaded].count === 1){
                        loadCache[instance.partIterationId+'-'+instance.qualityLoaded].geometry.dispose();
                        loadCache[instance.partIterationId+'-'+instance.qualityLoaded].material.dispose();
                        loadCache[instance.partIterationId+'-'+instance.qualityLoaded] = null;
                        delete loadCache[instance.partIterationId+'-'+instance.qualityLoaded];
                    }else{
                        loadCache[instance.partIterationId+'-'+instance.qualityLoaded].count--;
                    }
                }
                instance.qualityLoaded = undefined;
                worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                setTimeout(callback,0);
                return;
            }

            if (directive.quality === instance.qualityLoaded) {
                _this.alreadySameQuality++;
                setTimeout(callback,0);
                return;
            }

            // Check if not available in memory
            if(loadCache[instance.partIterationId+'-'+directive.quality]){
                loadCache[instance.partIterationId+'-'+directive.quality].count++;
                loadedInstances.push({
                    id: directive.id,
                    partIterationId: instance.partIterationId,
                    quality: directive.quality,
                    geometry: loadCache[instance.partIterationId+'-'+directive.quality].geometry,
                    materials: loadCache[instance.partIterationId+'-'+directive.quality].material
                });
                instance.qualityLoaded = directive.quality;
                worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                setTimeout(callback,0);
                return;
            }

            // Else : load the instance
            var quality = instance.qualities[directive.quality];
            var texturePath = quality.substring(0, quality.lastIndexOf('/'));
            loaderManager.parseFile(quality,texturePath,{
                success:function(geometry, material){

                    if(loadCache[instance.partIterationId+'-'+directive.quality]){
                        loadCache[instance.partIterationId+'-'+directive.quality].count++;
                    }else{
                        geometry.computeFaceNormals();
                        geometry.computeVertexNormals();
                        loadCache[instance.partIterationId+'-'+directive.quality]={count:1,geometry:THREE.BufferGeometryUtils.fromGeometry(geometry),material:material};
                    }

                    _this.xhrsDone++;

                    loadedInstances.push({
                        id: directive.id,
                        partIterationId: instance.partIterationId,
                        quality: directive.quality,
                        geometry: loadCache[instance.partIterationId+'-'+directive.quality].geometry,
                        materials:  loadCache[instance.partIterationId+'-'+directive.quality].material
                    });

                    instance.qualityLoaded = directive.quality;
                    worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});

                    callback();
                }
            });
        }

        function findQualities(files) {
            var q = [];
            _(files).each(function (f) {
                q[f.quality] = '/files/' + f.fullName;
            });
            return q;
        }

        function findRadius(files) {
            var r = 0;
            _(files).each(function (f) {
                if(f.radius){
                    r = f.radius;
                }
            });
            return r || 1;
        }

        function adaptMatrix(matrix){
            return new THREE.Matrix4(matrix[0],matrix[1],matrix[2],matrix[3],
                matrix[4],matrix[5],matrix[6],matrix[7],
                matrix[8],matrix[9],matrix[10],matrix[11],
                matrix[12],matrix[13],matrix[14],matrix[15]);
        }

        function updateWorker() {
            if(evalRunning){
                return;
            }
            evalRunning=true;
            var sceneContext = App.sceneManager.getSceneContext();
            if(App.debug){console.log('[InstancesManager] Updating worker');}
            worker.postMessage({fn: 'context', obj: {
                camera: sceneContext.camPos,
                target: sceneContext.target || {},
                WorkerManagedValues: App.WorkerManagedValues,
                debug: App.debug
            }});
        }

        function loadPath(paths, callback){
            $.ajax({
                url:'/api/workspaces/' + APP_CONFIG.workspaceId + '/products/' + APP_CONFIG.productId + '/instances?configSpec='+window.configSpec,
                type:'POST',
                contentType:'application/json',
                dataType:'json',
                data:JSON.stringify({
                    paths:paths
                }),
                success:function(instances){

                    _.each(instances, function (instance) {

                        if(instancesIndexed[instance.id]){
                            worker.postMessage({fn: 'check', obj: instance.id});
                        }else{
                            instancesIndexed[instance.id]=instance;
                            instance.matrix = adaptMatrix(instance.matrix);
                            var radius =  findRadius(instance.files);
                            var instanceBox = new THREE.Box3().setFromCenterAndSize(new THREE.Vector3(radius/2,radius/2,radius/2),new THREE.Vector3(radius,radius,radius));
                            var cog = instanceBox.center().applyMatrix4(instance.matrix);
                            instance.currentQuality = undefined;
                            instance.qualities =  findQualities(instance.files);

                            worker.postMessage({
                                fn: 'addInstance',
                                obj: {
                                    id: instance.id,
                                    cog: cog,
                                    radius: radius,
                                    qualities: instance.qualities,
                                    checked: true
                                }
                            });
                        }
                    });

                    _this.planNewEval();
                    loaderIndicator.hide();
                    callback();
                }
            });

        }

        function unLoadPath(paths, callback) {
            $.ajax({
                url:'/api/workspaces/' + APP_CONFIG.workspaceId + '/products/' + APP_CONFIG.productId + '/instances?configSpec='+window.configSpec,
                type:'POST',
                contentType:'application/json',
                data:JSON.stringify({
                    paths:paths
                }),
                success:function(instances){
                    _.each(instances, function (instance) {
                        worker.postMessage({fn: 'unCheck', obj: instance.id});
                    });
                    _this.planNewEval();
                    callback();
                }});
        }


        this.loadQueue=async.queue(function (directive, callback){
            if (directive.process === 'load'){
                loadPath(directive.path, callback);
            }else {
                unLoadPath(directive.path, callback);
            }
        },1);

        this.loadQueue.drain = function () {
            if(App.debug){console.log('[InstancesManager - loadQueue] All paths have been processed');}
        };

        this.loadQueue.empty = function () {
            if(App.debug){console.log('[InstancesManager - loadQueue] Queue is empty');}
        };

        this.loadQueue.saturated = function () {
            if(App.debug){console.log('[InstancesManager - loadQueue] Queue is saturated');}
        };


        this.xhrQueue=async.queue(loadProcess,4);

        this.xhrQueue.drain = function () {
            if(App.debug){console.log('[InstancesManager] All items have been processed');}
        };
        this.xhrQueue.empty = function () {
            if(App.debug){console.log('[InstancesManager] Queue is empty');}
        };
        this.xhrQueue.saturated = function () {
            if(App.debug){console.log('[InstancesManager] Queue is saturated');}
        };

        this.getLoadedGeometries = function (n) {
            return loadedInstances.splice(0, n || 1);
        };

        this.loadComponent = function(component) {
            loaderIndicator.show();
            _this.loadQueue.push({'process':'load','path':[component.getPath()]});
        };

        this.unLoadComponent = function(component) {
            _this.loadQueue.push({'process':'unload','path':[component.getPath()]});
        };

        this.clear = function(){
            if(App.debug){
	            console.log('[InstanceManager] Clearing Scene');
            }

            _this.xhrQueue.kill();
            _this.loadQueue.kill();

            _(_(instancesIndexed).pluck('id')).map(App.sceneManager.removeMeshById);

            _(loadCache).each(function(cache){
                cache.geometry.dispose();
                cache.material.dispose();
            });

            worker.postMessage({fn: 'clear', obj: null});

            instancesIndexed={};
            loadCache={};
            loadedInstances = [];
        };

        this.planNewEval= function(){
            clearTimeout(timer);
            if(!evalRunning){
                updateWorker();
            }else{
                timer = setTimeout(updateWorker, 500);
            }
        };

        this.getInstance = function(instanceId){
            return instancesIndexed[instanceId];
        };
    };

    return InstancesManager;
});