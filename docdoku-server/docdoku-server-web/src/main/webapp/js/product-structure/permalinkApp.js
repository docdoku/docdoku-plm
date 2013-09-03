var sceneManager;

define(["models/part_iteration_visualization","dmu/SceneManager"],function(PartIteration,SceneManager){

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
            this.loaderManager.parseFile(fileName, "", false, function(mesh){
                THREE.GeometryUtils.center(mesh.geometry);
                self.addMesh(mesh);
                self.camera.lookAt(mesh.position);
            });
        };

        sceneManager = new SceneManager();
        sceneManager.initPermalinkScene();
    }

    return PermalinkApp;

});
