var sceneManager,instancesManager;

define(["models/part_iteration_visualization","dmu/SceneManager","dmu/InstancesManager"],function(PartIteration,SceneManager,InstancesManager){

    function PermalinkApp(fileName, width, height){

        this.fileName = fileName;

        SceneManager.prototype.initRenderer = function() {
            this.renderer = new THREE.WebGLRenderer();
            this.renderer.setSize(width, height);
            this.$container.append(this.renderer.domElement);
        };

        SceneManager.prototype.initPermalinkScene = function() {
            this.init();
            var self = this;
            instancesManager.loadMeshFromFile(fileName, function(mesh){

                THREE.GeometryUtils.center(mesh.geometry);
                mesh.initialPosition=mesh.position;

                self.addMesh(mesh);
                self.camera.lookAt(mesh.position);
            });
        };

        instancesManager = new InstancesManager();
        instancesManager.init();
        sceneManager = new SceneManager();
        sceneManager.initPermalinkScene();

    }

    return PermalinkApp;

});
