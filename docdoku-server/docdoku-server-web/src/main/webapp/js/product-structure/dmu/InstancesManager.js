/*global sceneManager*/

define(["models/part_iteration_visualization", "dmu/LoaderManager"], function (PartIterationVisualization, LoaderManager) {

    /*
     *  This class handles instances management.
     *
     *  Dialog to sceneManager  : add and remove meshes
     *  Dialog from sceneManager  : init and update camera and frustum
     *
     * Dialog with worker :
      *  - insert and remove instances from tree,
      *  - update frustum and camera
      *
      * Dialog from worker
      *  - show/hide instances and update instances quality
      *
     * */

    var instancesIndexed=[];

    var disposeQueue = {
        _q:[],
        disposeInALoop:8,
        add:function(e){
            disposeQueue._q.push(e);
        },
        _run:function(object){
            if(object){
                object.traverse( function ( child ) {
                    if ( child.geometry !== undefined ) {
                        child.geometry.dispose();
                        child.material.dispose();
                    }
                });
            }
            object=null;
        },
        run:function(){
            for(var i = 0 ; i < disposeQueue.disposeInALoop ; i++){
                disposeQueue._run(disposeQueue._q.shift());
            }
        }
    };

    var queue = {
        max:4,
        count:0,
        _q:[],
        watch:function(){
            if(queue.count < queue.max){
                queue.next();
            }
        },
        add:function(e){
            queue._q.push(e);
        },
        next:function(){
            var n = queue._q.shift();
            if(n){
                queue.count++;
                try{
                    n();
                }catch(e){
                    console.log("Exception while executing a task : ");
                    console.log(e);
                    queue.count--;
                }
            }
        },
        taskOver:function(){
            queue.count--;
            queue.next();
        }
    };

    function cleanAndRemoveMesh(object){
        disposeQueue.add(object);
        sceneManager.scene.remove(object);
    }

    function applyMatrix(instanceRaw){
        var m = new THREE.Matrix4(instanceRaw.matrix[0],instanceRaw.matrix[1],instanceRaw.matrix[2],instanceRaw.matrix[3],instanceRaw.matrix[4],instanceRaw.matrix[5],instanceRaw.matrix[6],instanceRaw.matrix[7],instanceRaw.matrix[8],instanceRaw.matrix[9],instanceRaw.matrix[10],instanceRaw.matrix[11],instanceRaw.matrix[12],instanceRaw.matrix[13],instanceRaw.matrix[14],instanceRaw.matrix[15]);
        instanceRaw.mesh.applyMatrix(m);
    }

    var findQualities = function (files) {
        var q = [];
        _(files).each(function (f) {
            q[f.quality] = "/files/" + f.fullName;
        });
        return q;
    };
    var findRadius = function (files) {
        var r = 0;
        _(files).each(function (f) {
           if(f.radius){
               r = f.radius;
           }
        });
        return r || 1;
    };
    var InstancesManager = function () {
        this.partIterations = [];
        this.loaderManager = null;
        this.workerURI = "/js/product-structure/workers/InstancesWorker.js";
    };

    InstancesManager.prototype = {

        dequeue:function(){
            disposeQueue.run();
            queue.watch();
        },

        init: function () {
            var self = this ;
            this.loaderManager = new LoaderManager({progressBar: true});
            this.worker = new Worker(this.workerURI);
            this.worker.addEventListener("message", function(message){
                self.onWorkerMessage(message);
            });
            this.loaderIndicator = $("#product_title > img.loader");
        },

        loadMeshFromFile: function (fileName, callback) {
            var texturePath = fileName.substring(0, fileName.lastIndexOf('/'));
            this.loaderManager.parseFile(fileName, texturePath, callback);
        },

        loadFromTree: function (component) {

            var self = this;

            this.loaderIndicator.show();

            $.getJSON(component.getInstancesUrl(), function (instances) {

                _.each(instances, function (instanceRaw) {

                    if(instancesIndexed[instanceRaw.id]){
                        self.worker.postMessage({
                            check:instanceRaw.id
                        });
                        return ;
                    }

                    instancesIndexed[instanceRaw.id]=instanceRaw;

                    /*
                     * Init the mesh with empty geometry
                     * */

                    var mesh = new THREE.Mesh(new THREE.Geometry(),new THREE.MeshPhongMaterial());

                    instanceRaw.mesh = mesh;
                    mesh._uuid = instanceRaw.id;
                    instanceRaw.currentQuality = null;
                    instanceRaw.mesh.partIterationId = instanceRaw.partIterationId;

                    applyMatrix(instanceRaw);

                    instanceRaw.position=mesh.position.clone();
                    var qualities =  findQualities(instanceRaw.files);
                    var radius =  findRadius(instanceRaw.files);

                    var cog = {
                        x:mesh.position.x+radius,
                        y:mesh.position.y+radius,
                        z:mesh.position.z+radius
                    };

                    self.worker.postMessage({
                        mesh: {
                            uuid: mesh._uuid,
                            cog: cog,
                            radius: radius,
                            qualities:qualities
                        }
                    });

                });

                self.loaderIndicator.hide();

            });
        },

        unLoadFromTree: function (component) {
            var self = this;
            $.getJSON(component.getInstancesUrl(), function (instances) {
                _.each(instances, function (instanceRaw) {
                    self.worker.postMessage({unCheck:instanceRaw.id});
                });
            });
        },

        updateWorker: function (cameraPosition, target) {

            // Finish current work before, maybe we would abort all tasks instead ?
            if(queue.count){
                return;
            }

            this.worker.postMessage({context: {camera: cameraPosition, target: target || {} }});
        },

        clear:function(){
            this.worker.postMessage({clear:true});
            for(var i in instancesIndexed){
                var instance = instancesIndexed[i];
                cleanAndRemoveMesh(instance.mesh);
            }
            instancesIndexed=[];
        },

        onWorkerMessage: function (message) {

            var self = this ;

            queue.add(function () {

                var uuid = message.data.uuid;
                var quality = message.data.quality;

                var instance = instancesIndexed[uuid];

                if (!instance) {
                    queue.taskOver();
                    return;
                }

                // First of all, check if quality sent is different from current one

                /*
                 * Switching geometry
                 * */

                if(quality !== instance.currentQuality){

                    if(instance.mesh){
                        cleanAndRemoveMesh(instance.mesh);
                    }

                    if(quality != null){
                        var texturePath = quality.substring(0, quality.lastIndexOf('/'));
                        self.loaderManager.parseFile(quality, texturePath, function (mesh) {

                            instance.mesh = mesh;
                            instance.currentQuality = quality;
                            instance.mesh._uuid = uuid;
                            instance.overall=message.data.overall;
                            instance.mesh.geometry.verticesNeedUpdate=true;
                            instance.mesh.partIterationId = instance.partIterationId;

                            applyMatrix(instance);

                            sceneManager.addMesh(instance.mesh);

                            queue.taskOver();
                        });
                    }else{
                        // Mesh should be removed by cleanAndRemoveMesh
                        // Do nothing
                        instance.currentQuality = null;
                        queue.taskOver();
                    }
                }else{
                    cleanAndRemoveMesh(instance.mesh);
                    // Didn't changed ???
                    queue.taskOver();
                }

            });


        }

    };

    return InstancesManager;

});
