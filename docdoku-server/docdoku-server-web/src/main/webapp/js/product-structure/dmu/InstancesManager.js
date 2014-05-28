/*global App*/

define(["dmu/LoaderManager","lib/async"],
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
        var worker = new Worker("/js/product-structure/workers/InstancesWorker.js");
        worker.addEventListener("message", function (message) {
            if (typeof  workerMessages[message.data.fn] == "function") {
                workerMessages[message.data.fn](message.data.obj);
            } else {
                if(App.debug){
                    console.log("[InstancesManager] Unrecognized command  : ");
                    console.log(message.data);
                }
            }
        }, false);


        this.trashInstances = [];                                                                                       // Index isntances for removal
        this.xhrQueue=null;
        // Stat to show
        this.aborted = 0;
        this.alreadySameQuality = 0;
        this.errors = 0;
        this.xhrsDone = 0;

        var instancesIndexed=[];
        var loadedInstances = [];                                                                                       // Store all loaded geometries and materials
        var loaderManager = new LoaderManager({progressBar: true});
        var loaderIndicator = $("#product_title").find("img.loader");

        var currentDirectivesCount = 0;
        var needsWorkerEval = false;
        var evalRunning = false;
        var timer = null;
        var ticker = null;                                                                                              // Timer for ticker
        var aborting = false;

        var workerMessages = {
            stats: function (stats) {
                _this.workerStats = stats;
            },
            directives: function (directives) {
                currentDirectivesCount = directives.length;
                if (currentDirectivesCount) {
                    directives.forEach(function (directive) {

                        var instance = _this.getInstance(directive.id);
                        instance.directiveQuality = directive.quality;
                        _this.xhrQueue.push(directive);

                    });
                }
                setTimeout(function(){
                    evalRunning = false;
                },200);
            }
        };

        this.xhrQueue=async.queue(loadProcess,4);
        this.xhrQueue.drain = function () {
            if(App.debug){console.log('[InstancesManager] All items have been processed');}
            currentDirectivesCount=0;
        };
        this.xhrQueue.empty = function () {
            if(App.debug){console.log('[InstancesManager] Queue is empty');}
        };
        this.xhrQueue.saturated = function () {
            if(App.debug){console.log('[InstancesManager] Queue is saturated');}
        };

        /**
         * Load process : xhr + store geometry and materials in array
         */
        function loadProcess(directive, callback){
            var instance = _this.getInstance(directive.id);
            if(!instance){
                setTimeout(callback,0);
                return;
            }
            if (directive.quality == undefined) {
                _this.trashInstances.push(instance);
                setTimeout(callback,0);
                return;
            }
            if (directive.quality == instance.qualityLoaded) {
                _this.alreadySameQuality++;
                setTimeout(callback,0);
                return;
            }
            if (aborting) {
                _this.aborted++;
                worker.postMessage({fn:"abort",obj:{id:instance.id,quality:instance.qualityLoaded}});
                setTimeout(callback,0);
                return;
            }

            // Else : load the instance
            var quality = instance.qualities[directive.quality];
            var texturePath = quality.substring(0, quality.lastIndexOf('/'));
            loaderManager.parseFile(quality,texturePath,{
                success:function(geometry, material){
                    _this.xhrsDone++;
                    geometry.computeFaceNormals();
                    geometry.computeVertexNormals();

                    loadedInstances.push({
                        id: directive.id,
                        partIterationId: instance.partIterationId,
                        quality: directive.quality,
                        geometry: THREE.BufferGeometryUtils.fromGeometry(geometry),
                        materials: material
                    });
                    callback();
                }
            });
        }

        function findQualities(files) {
            var q = [];
            _(files).each(function (f) {
                q[f.quality] = "/files/" + f.fullName;
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
        /**
         * Update worker context
         */
        function updateWorker() {
            evalRunning=true;
            currentDirectivesCount=0;
            var sceneContext = App.sceneManager.getSceneContext();
            if(App.debug){console.log("[InstancesManager] Updating worker");}
            worker.postMessage({fn: "context", obj: {
                camera: sceneContext.camPos,
                target: sceneContext.target || {},
                WorkerManagedValues: App.WorkerManagedValues,
                debug: App.debug
            }});
        }
        function tick(){
            if(!needsWorkerEval || evalRunning){return;}                                                                // Nothing to load or eval is currently running

            var allFinished = !_this.xhrQueue.running() && !_this.xhrQueue.length() && !loadedInstances.length;
            // Wait for abort
            if(aborting){
                aborting = !allFinished;
                return;
            }

            // Need to abort if any
            if(!allFinished){
                if(App.debug){
                    console.log("[InstancesManager] Aborting remaining tasks now");
                    console.log("[InstancesManager] Tasks left : "+_this.xhrQueue.length()+"  tasks running "+_this.xhrQueue.running());
                }
                aborting=true;
                _this.xhrQueue.concurrency = Infinity;
                return;
            }

            _this.xhrQueue.concurrency = 4;

            // Abort over, eval is ready. Program an update
            if(needsWorkerEval){
                needsWorkerEval=false;
                clearTimeout(timer);
                timer = setTimeout(updateWorker,500);
            }
        }

        this.getLoadedGeometries = function (n) { return loadedInstances.splice(0, n || 1); };
        this.getTrash = function () { return _this.trashInstances; };
        this.clearTrash = function () { _this.trashInstances = [];};

        this.start = function(){
            needsWorkerEval = true;
            clearInterval(ticker);
            ticker = setInterval(tick,200);
        };
        this.loadMeshFromFile = function(fileName, callback){
            var texturePath = fileName.substring(0, fileName.lastIndexOf('/'));
            loaderManager.parseFile(fileName, texturePath, {
                success:function(geometry, material){
                    callback(new THREE.Mesh(geometry, material));
                }
            });
        };
        this.loadFromTree = function(component) {
            loaderIndicator.show();

            $.getJSON(component.getInstancesUrl(), function (instances) {
                _.each(instances, function (instanceRaw) {
                    if(instancesIndexed[instanceRaw.id]){
                        worker.postMessage({fn: "check", obj: instanceRaw.id});
                        return ;
                    }else{
                        instanceRaw.matrix = adaptMatrix(instanceRaw.matrix);
                    }
                    instancesIndexed[instanceRaw.id]=instanceRaw;
                    /*
                     * Init the mesh with empty geometry
                     * */
                    var mesh = new THREE.Mesh(new THREE.Geometry(),new THREE.MeshPhongMaterial());
                    instanceRaw.mesh = mesh;
                    instanceRaw.currentQuality = null;
                    instanceRaw.mesh.partIterationId = instanceRaw.partIterationId;
                    instanceRaw.mesh.applyMatrix(instanceRaw.matrix);
                    instanceRaw.position=mesh.position.clone();
                    instanceRaw.qualities =  findQualities(instanceRaw.files);
                    var radius =  findRadius(instanceRaw.files);

                    var cog = {
                        x:mesh.position.x+radius,
                        y:mesh.position.y+radius,
                        z:mesh.position.z+radius
                    };

                    worker.postMessage({
                        fn: "addInstance",
                        obj: {
                            id: instanceRaw.id,
                            cog: cog,
                            radius: radius,
                            qualities: instanceRaw.qualities,
                            checked: true
                        }
                    });
                });
                _this.planNewEval();
                loaderIndicator.hide();
            });
        };
        this.unLoadFromTree = function(component) {
            $.getJSON(component.getInstancesUrl(), function (instances) {
                _.each(instances, function (instanceRaw) {
                    instanceRaw.checked = false;
                    _this.trashInstances.push(instanceRaw);
                    worker.postMessage({fn: "unCheck", obj: instanceRaw.id});
                });
                _this.planNewEval();
            });
        };
        this.clear = function(){
            for(var i in instancesIndexed){
                var instance = instancesIndexed[i];
                _this.trashInstances.push(instance);
            }
            worker.postMessage({fn: "clear", obj: null});
            instancesIndexed=[];
        };
        this.planNewEval= function(){                                                                                   // Launch a new eval on the worker when you can
            needsWorkerEval=true;
        };
        this.getInstance = function(instanceId){
            return instancesIndexed[instanceId];
        };
    };

    return InstancesManager;
});