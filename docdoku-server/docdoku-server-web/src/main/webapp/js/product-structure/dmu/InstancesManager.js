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
                _.each(instances, function (instanceRaw) {
                    var partIteration = self.getOrCreatePartIteration(instanceRaw).addInstance(instanceRaw);
                    if (partIteration.hasGeometry()) {
                        self.onInstanceAdded(partIteration.getInstance(instanceRaw.id));
                    }
                });
                self.loaderIndicator.hide();
                setTimeout(function(){self.updateWorker();}, 2);
            });
        },

        onInstanceAdded: function (instance) {
            this.worker.postMessage(JSON.stringify({fn: "insert", instance: instance.toCircularDataSafe()}));
        },

        unLoadFromTree: function (component) {
            var self = this;
            $.getJSON(component.getInstancesUrl(), function (instances) {
                _.each(instances, function (instanceRaw) {
                    var partIteration = self.getPartIteration(instanceRaw.partIterationId);
                    if (partIteration && partIteration.hasGeometry()) {
                        self.onInstanceRemoved(instanceRaw.id);
                        partIteration.removeInstance(instanceRaw.id);
                    }
                });
                setTimeout(function(){self.updateWorker();}, 50);
            });
        },

        onInstanceRemoved: function (instanceId) {
            this.worker.postMessage(JSON.stringify({fn: "remove", instanceId:instanceId}));
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

            // Show instances on scene
            if(data.fn == "show"){
                _(data.instances).each(function (instance) {
                    self.getPartIteration(instance.partIterationId).showInstance(instance);
                });
            }

            // Remove instances on scene
            else if(data.fn == "remove"){
                _(data.instances).each(function (instance) {
                    self.getPartIteration(instance.partIterationId).removeInstance(instance.id);
                });
            }

            // Update instances quality
            else if(data.fn == "update"){
                _(data.instances).each(function (instance) {
                    self.getPartIteration(instance.partIterationId).showInstance(instance);
                });
            }

            // Debug worker
            else if(data.fn == "debug"){
               console.log(data);
            }

        }

    };

    return InstancesManager;

})