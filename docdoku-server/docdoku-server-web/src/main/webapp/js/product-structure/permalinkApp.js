var sceneManager,instancesManager;

define(["models/part_iteration_visualization","dmu/SceneManager","dmu/InstancesManager"],function(PartIteration,SceneManager,InstancesManager){

    function PermalinkApp(fileName, width, height){

        this.fileName = fileName;

        SceneManager.prototype.initRenderer = function() {
            this.renderer = new THREE.WebGLRenderer({alpha: true});
            this.renderer.setSize(width, height);
            this.$container.append(this.renderer.domElement);
        };

        instancesManager = new InstancesManager();
        sceneManager = new SceneManager();

        instancesManager.init();
        sceneManager.init();

        instancesManager.loadMeshFromFile(fileName, function(mesh){
            THREE.GeometryUtils.center(mesh.geometry);
            mesh.initialPosition=mesh.position;
            sceneManager.addMesh(mesh);
            sceneManager.camera.lookAt(mesh.position);
        });

    }

    return PermalinkApp;

});
