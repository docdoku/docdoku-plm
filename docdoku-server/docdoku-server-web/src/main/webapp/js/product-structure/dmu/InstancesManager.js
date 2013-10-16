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

    var InstancesManager = function () {
        this.partIterations = [];
        this.loaderManager = null;
        this.workerURI = "/js/product-structure/workers/InstancesWorker.js";
    };

    InstancesManager.prototype = {

        init: function () {
            var self = this ;
            this.loaderManager = new LoaderManager({progressBar: true});
            this.worker = new Worker(this.workerURI);
            this.worker.addEventListener("message", function(message){
                self.onWorkerMessage(message);
            });
            this.loaderIndicator = $("#product_title > img.loader");
        },

        addPartIteration: function (partIteration) {
            this.partIterations[partIteration.id] = partIteration;
        },

        getPartIteration: function (partIterationId) {
            return this.partIterations[partIterationId];
        },

        getOrCreatePartIteration: function (instanceRaw) {
            if (!this.hasPartIteration(instanceRaw.partIterationId)) {
                this.addPartIteration(new PartIterationVisualization(instanceRaw));
            }
            return this.getPartIteration(instanceRaw.partIterationId);
        },

        hasPartIteration: function (partIterationId) {
            return this.partIterations[partIterationId] !== undefined;
        },

        loadMeshFromFile: function (fileName, callback) {
            var texturePath = fileName.substring(0, fileName.lastIndexOf('/'));
            this.loaderManager.parseFile(fileName, texturePath, callback);
        },

        loadFromTree: function (component) {
            var self = this;
            this.loaderIndicator.show();
            $.getJSON(component.getInstancesUrl(), function (instances) {

                var instancesToInsert=[];
                _.each(instances, function (instanceRaw) {
                    var partIteration = self.getOrCreatePartIteration(instanceRaw).addInstance(instanceRaw);
                    if (partIteration.hasGeometry()) {
                        instancesToInsert.push(partIteration.getInstance(instanceRaw.id));
                    }
                });

                // Send to worker
                if(instancesToInsert.length){
                    self.onInstancesAdded(instancesToInsert);
                }

                self.loaderIndicator.hide();

            });
        },

        onInstancesAdded: function (instances) {
            this.worker.postMessage(JSON.stringify({fn: "insert", instances: instances}));
        },

        unLoadFromTree: function (component) {
            var self = this;
            $.getJSON(component.getInstancesUrl(), function (instances) {
                var instancesToRemove=[];
                _.each(instances, function (instanceRaw) {
                    var partIteration = self.getPartIteration(instanceRaw.partIterationId);
                    if (partIteration && partIteration.hasGeometry()) {
                        instancesToRemove.push(instanceRaw.id);
                        partIteration.hideInstance(instanceRaw);
                    }
                });

                self.onInstancesRemoved(instancesToRemove);
            });
        },

        onInstancesRemoved: function (instancesId) {
            this.worker.postMessage(JSON.stringify({fn: "remove", instancesId:instancesId}));
        },

        debugWorker: function () {
            this.worker.postMessage(JSON.stringify({fn: "debug"}));
        },

        initWorker: function () {
            this.worker.postMessage(JSON.stringify({
                fn: "init",
                frustum: sceneManager.frustum,
                cameraPosition: sceneManager.cameraPosition
            }));
        },

        updateWorker: function () {
            sceneManager.frustum.setFromMatrix(new THREE.Matrix4().multiplyMatrices(sceneManager.camera.projectionMatrix, sceneManager.camera.matrixWorldInverse));
            this.worker.postMessage(JSON.stringify({
                fn: "update",
                frustum: sceneManager.frustum,
                cameraPosition: sceneManager.cameraPosition
            }));
        },

        onWorkerMessage: function (message) {

            var data = JSON.parse(message.data);
            var self = this;

            // Remove instances from scene
            if(data.fn == "hide"){
                _(data.instances).each(function (instance) {
                    self.getPartIteration(instance.partIterationId).hideInstance(instance);
                });
            }
            // Show instances on scene
            else  if(data.fn == "show"){
                _(data.instances).each(function (instance) {
                    self.getPartIteration(instance.partIterationId).showInstance(instance);
                });
            }
            // Update instances quality
            else if(data.fn == "update"){
                _(data.instances).each(function (instance) {
                    self.getPartIteration(instance.partIterationId).updateInstance(instance);
                });
            }
            // Debug worker
            else if(data.fn == "debug"){
                console.log(data);
                console.log("on scene " + (sceneManager.scene.children.length - 5));
                console.log(sceneManager.renderer.info.render);
            }
        }

    };

    return InstancesManager;

});
