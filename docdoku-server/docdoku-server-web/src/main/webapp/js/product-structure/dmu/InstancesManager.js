/*global sceneManager, APP*/

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

        this.loadedInstances = [];                                                                                      // Store all loaded geometries and materials
        this.trashInstances = [];                                                                                       // Index isntances for removal
        this.xhrQueue=null;
        // Stat to show
        this.aborted = 0;
        this.alreadySameQuality = 0;
        this.errors = 0;
        this.xhrsDone = 0;

        this.partIterations = [];
        var loaderManager = new LoaderManager({progressBar: true});
        var loaderIndicator = $("#product_title > img.loader");

        var currentDirectivesCount = 0;
        var needsWorkerEval = false;
        var evalRunning = false;
        var instancesIndexed=[];
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
            loaderManager.parseFile(quality,texturePath,function(mesh){
                _this.xhrsDone++;

                mesh.uuid = instance.id;
                mesh.geometry.verticesNeedUpdate=true;
                mesh.partIterationId = instance.partIterationId;
                applyMatrix(mesh,instance.matrix);
                _this.loadedInstances.push({
                    id: directive.id,
                    quality: directive.quality,
                    mesh: mesh
                });
                callback();
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
        function cleanAndRemoveMesh(object){
            sceneManager.scene.remove(object);
        }
        function applyMatrix(mesh,matrix){
            var m = new THREE.Matrix4(matrix[0],matrix[1],matrix[2],matrix[3],
                                      matrix[4],matrix[5],matrix[6],matrix[7],
                                      matrix[8],matrix[9],matrix[10],matrix[11],
                                      matrix[12],matrix[13],matrix[14],matrix[15]);
            mesh.applyMatrix(m);
        }
        /**
         * Update worker context
         */
        function updateWorker() {
            evalRunning=true;
            currentDirectivesCount=0;
            var sceneContext = sceneManager.getSceneContext();
            if(App.debug){console.log("[InstancesManager] Updating worker");}
            worker.postMessage({fn: "context", obj: {
                camera: sceneContext.camPos,
                target: sceneContext.target || {},
                WorkerManagedValues: App.WorkerManagedValues,
                debug: App.debug
            }});
            /*worker.postMessage({context:
                                        {camera: sceneContext.camPos,
                                            target: sceneContext.target || {} }});*/
        }
        function tick(){
            if(!needsWorkerEval || evalRunning){return;}                                                                // Nothing to load or eval is currently running

            var allFinished = !_this.xhrQueue.running() && !_this.xhrQueue.length() && !_this.loadedInstances.length;
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

        this.getLoadedGeometries = function (n) { return _this.loadedInstances.splice(0, n || 1); };
        this.getTrash = function () { return _this.trashInstances; };
        this.clearTrash = function () { _this.trashInstances = [];};

        this.start = function(){
            needsWorkerEval = true;
            clearInterval(ticker);
            ticker = setInterval(tick,200);
        };
        this.loadMeshFromFile = function(fileName, callback){
            var texturePath = fileName.substring(0, fileName.lastIndexOf('/'));
            loaderManager.parseFile(fileName, texturePath, callback);
        };
        this.loadFromTree = function(component) {
            loaderIndicator.show();

            $.getJSON(component.getInstancesUrl(), function (instances) {
                _.each(instances, function (instanceRaw) {
                    if(instancesIndexed[instanceRaw.id]){
                        worker.postMessage({fn: "check", obj: instanceRaw.id});
                        //worker.postMessage({check:instanceRaw.id});
                        return ;
                    }
                    instancesIndexed[instanceRaw.id]=instanceRaw;
                    /*
                     * Init the mesh with empty geometry
                     * */
                    var mesh = new THREE.Mesh(new THREE.Geometry(),new THREE.MeshPhongMaterial());
                    instanceRaw.mesh = mesh;
                    instanceRaw.currentQuality = null;
                    instanceRaw.mesh.partIterationId = instanceRaw.partIterationId;

                    applyMatrix(instanceRaw.mesh,instanceRaw.matrix);

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
            worker.postMessage({fn: "clear", obj: null});
            //worker.postMessage({clear:true});
            for(var i in instancesIndexed){
                var instance = instancesIndexed[i];
                cleanAndRemoveMesh(instance.mesh);
            }
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