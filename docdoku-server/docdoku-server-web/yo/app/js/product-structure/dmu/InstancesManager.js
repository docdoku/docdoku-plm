/*global _,$,define,App,THREE,Worker*/
define(['dmu/LoaderManager', 'async'],
    function (LoaderManager, async) {
	    'use strict';
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

            this.xhrQueue = null;
            this.loadQueue = null;
            this.aborted = 0;
            this.alreadySameQuality = 0;
            this.xhrsDone = 0;

            var instancesIndexed = {};
            var loadCache = {};
            var loadedInstances = [];                                                                                   // Store all loaded geometries and materials
            var loaderManager = new LoaderManager({progressBar: true});
            var loaderIndicator = $('#product_title').find('img.loader');
            var timer = null;
            var evalRunning = false;

	        var worker = new Worker(App.config.contextPath + '/js/product-structure/workers/InstancesWorker.js');

            var workerMessages = {
                stats: function (stats) {
                    _this.workerStats = stats;
                },
                directives: function (directives) {
                    _this.aborted += _this.xhrQueue.tasks.length;
                    _this.xhrQueue.kill();

                    _(directives).each(function (directive) {
                        var instance = _this.getInstance(directive.id);
                        if (directive.nowait && directive.quality === undefined) {
                            App.sceneManager.removeMeshById(directive.id);
                            if (loadCache[instance.partIterationId + '-' + instance.qualityLoaded]) {
                                if (loadCache[instance.partIterationId + '-' + instance.qualityLoaded].count === 1) {
                                    loadCache[instance.partIterationId + '-' + instance.qualityLoaded].geometry.dispose();
                                    loadCache[instance.partIterationId + '-' + instance.qualityLoaded].material.dispose();
                                    loadCache[instance.partIterationId + '-' + instance.qualityLoaded] = null;
                                    delete loadCache[instance.partIterationId + '-' + instance.qualityLoaded];
                                } else {
                                    loadCache[instance.partIterationId + '-' + instance.qualityLoaded].count--;
                                }
                            }
                            instance.qualityLoaded = undefined;
                            worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                        } else {
                            instance.directiveQuality = directive.quality;
                            _this.xhrQueue.push(directive);
                        }
                    });


                    setTimeout(function () {
                        evalRunning = false;
                    }, 500);
                }
            };

            worker.addEventListener('message', function (message) {
                if (typeof  workerMessages[message.data.fn] === 'function') {
                    workerMessages[message.data.fn](message.data.obj);
                } else {
                    App.log('%c Unrecognized command  : \n\t'+message.data,'IM');
                }
            }, false);

            /**
             * Load process : xhr + store geometry and materials in array
             */
            function loadProcess(directive, callback) {
                var instance = _this.getInstance(directive.id);
                if (!instance) {
                    setTimeout(callback, 0);
                    return;
                }

                if (directive.quality === undefined) {

                    // don't unload edited meshes
                    if (App.sceneManager.editedMeshes.indexOf(instance.id) !== -1) {
                        _this.aborted++;
                        worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                        setTimeout(callback, 0);
                        return;
                    }

                    App.sceneManager.removeMeshById(instance.id);
                    if (loadCache[instance.partIterationId + '-' + instance.qualityLoaded]) {
                        if (loadCache[instance.partIterationId + '-' + instance.qualityLoaded].count === 1) {
                            loadCache[instance.partIterationId + '-' + instance.qualityLoaded].geometry.dispose();
                            loadCache[instance.partIterationId + '-' + instance.qualityLoaded].material.dispose();
                            loadCache[instance.partIterationId + '-' + instance.qualityLoaded] = null;
                            delete loadCache[instance.partIterationId + '-' + instance.qualityLoaded];
                        } else {
                            loadCache[instance.partIterationId + '-' + instance.qualityLoaded].count--;
                        }
                    }
                    instance.qualityLoaded = undefined;
                    worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                    setTimeout(callback, 0);
                    return;
                }

                if (directive.quality === instance.qualityLoaded) {
                    _this.alreadySameQuality++;
                    setTimeout(callback, 0);
                    return;
                }

                // Check if not available in memory
                if (loadCache[instance.partIterationId + '-' + directive.quality]) {
                    loadCache[instance.partIterationId + '-' + directive.quality].count++;
                    loadedInstances.push({
                        id: directive.id,
                        partIterationId: instance.partIterationId,
                        quality: directive.quality,
                        geometry: loadCache[instance.partIterationId + '-' + directive.quality].geometry,
                        materials: loadCache[instance.partIterationId + '-' + directive.quality].material
                    });
                    instance.qualityLoaded = directive.quality;
                    worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                    setTimeout(callback, 0);
                    return;
                }

                // Else : load the instance
                var quality = App.config.contextPath+'/'+instance.files[directive.quality].fullName;

                var texturePath = quality.substring(0, quality.lastIndexOf('/'));
                loaderManager.parseFile(quality, texturePath, {
                    success: function (geometry, material) {

                        if (loadCache[instance.partIterationId + '-' + directive.quality]) {
                            loadCache[instance.partIterationId + '-' + directive.quality].count++;
                        } else {
                            geometry.computeFaceNormals();
                            //geometry.computeVertexNormals();
                            loadCache[instance.partIterationId + '-' + directive.quality] = {count: 1, geometry: geometry, material: material};
                        }

                        _this.xhrsDone++;

                        loadedInstances.push({
                            id: directive.id,
                            partIterationId: instance.partIterationId,
                            quality: directive.quality,
                            geometry: loadCache[instance.partIterationId + '-' + directive.quality].geometry,
                            materials: loadCache[instance.partIterationId + '-' + directive.quality].material
                        });

                        instance.qualityLoaded = directive.quality;
                        worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});

                        callback();
                    }
                });
            }

            function adaptMatrix(matrix) {
                return new THREE.Matrix4(matrix[0], matrix[1], matrix[2], matrix[3],
                    matrix[4], matrix[5], matrix[6], matrix[7],
                    matrix[8], matrix[9], matrix[10], matrix[11],
                    matrix[12], matrix[13], matrix[14], matrix[15]);
            }

            function updateWorker() {
                if (evalRunning) {
                    return;
                }
                evalRunning = true;
                var sceneContext = App.sceneManager.getControlsContext();
                App.log('%c Updating worker', 'IM');
                worker.postMessage({fn: 'context', obj: {
                    camera: sceneContext.camPos,
                    target: sceneContext.target || {},
                    WorkerManagedValues: App.WorkerManagedValues,
                    debug: App.debug
                }});

                if(App.router){
                    App.router.updateRoute(sceneContext);
                }
            }

	        function onSuccessLoadPath(instances){
		        _.each(instances, function (instance) {
			        if (instancesIndexed[instance.id]) {
				        worker.postMessage({fn: 'check', obj: instance.id});
			        } else {

				        instancesIndexed[instance.id] = instance;
				        instance.matrix = adaptMatrix(instance.matrix);

                        var min =  new THREE.Vector3(instance.xMin,instance.yMin,instance.zMin);
                        var max = new THREE.Vector3(instance.xMax,instance.yMax,instance.zMax);
                        var box = new THREE.Box3(min,max).applyMatrix4(instance.matrix);

                        var cog = box.center();

                        // Allow parts that don't have box to be displayed
                        var radius = box.size().length() || 0.01;

                        worker.postMessage({
					        fn: 'addInstance',
					        obj: {
                                instanceRow:instance,
						        id: instance.id,
						        box: box,
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
	        }

	        function loadPath(path, callback) {
		        $.ajax({
			        url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' +
				         App.config.productId + '/instances' +
				         '?configSpec='+App.config.configSpec+'&path='+path,
			        type: 'GET',
			        success:function(instances){
				        onSuccessLoadPath(instances);
				        if(callback){
					        callback(instances);
				        }
			        }
		        });
	        }
            function loadPaths(paths, callback) {
                $.ajax({
	                url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' +
		                 App.config.productId + '/instances',
	                type:'POST',
	                contentType: 'application/json',
	                dataType: 'json',
	                data: JSON.stringify({
		                configSpec: App.config.configSpec,
		                paths: paths
	                }),
	                success:function(instances){
		                onSuccessLoadPath(instances);
		                if(callback){
			                callback(instances);
		                }
	                }
                });

            }
	        function unLoadPath(path, callback) {
		        $.ajax({
			        url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' +
				         App.config.productId + '/instances' +
				         '?configSpec=' + App.config.configSpec+'&path='+path,
			        type: 'GET',
			        success: function (instances) {
				        _.each(instances, function (instance) {
					        worker.postMessage({fn: 'unCheck', obj: instance.id});
				        });
				        _this.planNewEval();
				        callback();
			        }});
	        }

            this.loadQueue = async.queue(function (directive, callback) {
                if (directive.process === 'load') {
	                loadPaths(directive.paths, callback);
                } else if (directive.process === 'loadOne'){
	                loadPath(directive.path, callback);
                } else {
                    unLoadPath(directive.path, callback);
                }
            }, 1);

            this.loadQueue.drain = function () {
                App.log('Load Queue %c All paths have been processed','IM');
            };
            this.loadQueue.empty = function () {
                App.log('Load Queue %c  Empty Queue','IM');
            };
            this.loadQueue.saturated = function () {
                App.log('Load Queue %c Saturated Queue','IM');
            };


            this.xhrQueue = async.queue(loadProcess, 4);

            this.xhrQueue.drain = function () {
                App.log('XHR Queue %c All items have been processed','IM');
            };
            this.xhrQueue.empty = function () {
                App.log('XHR Queue %c Empty Queue','IM');
            };
            this.xhrQueue.saturated = function () {
                App.log('XHR Queue %c Saturated Queue','IM');
            };

            this.getLoadedGeometries = function (n) {
                return loadedInstances.splice(0, n || 1);
            };

            this.loadComponent = function (component) {
                loaderIndicator.show();
                _this.loadQueue.push({'process': 'loadOne', 'path': [component.getEncodedPath()]});
            };
	        this.loadComponentsByPaths = function(paths){
		        loaderIndicator.show();
		        var directive = {
			        process: 'load',
			        paths: []
		        };
		        _.each(paths,function(path){
			        directive.paths.push(path);
		        });
		        _this.loadQueue.push(directive);
	        };
            this.unLoadComponent = function (component) {
                _this.loadQueue.push({'process': 'unload', 'path': component.getEncodedPath()});
            };
	        this.unLoadComponentsByPaths = function (pathsToUnload) {
		        _(pathsToUnload).each(function(path){
			        _this.loadQueue.push({'process': 'unload', 'path': path});
		        });
	        };

            this.clear = function () {
                App.log('%c Clearing Scene','IM');

                _this.xhrQueue.kill();
                _this.loadQueue.kill();

                _(_(instancesIndexed).pluck('id')).map(App.sceneManager.removeMeshById);

                _(loadCache).each(function (cache) {
                    cache.geometry.dispose();
                    cache.material.dispose();
                });

                worker.postMessage({fn: 'clear', obj: null});

                instancesIndexed = {};
                loadCache = {};
                loadedInstances = [];
            };

            this.planNewEval = function () {
                clearTimeout(timer);
                if (!evalRunning) {
                    updateWorker();
                } else {
                    timer = setTimeout(updateWorker, 500);
                }
            };

            this.getInstance = function (instanceId) {
                return instancesIndexed[instanceId];
            };
        };

        return InstancesManager;
    });
