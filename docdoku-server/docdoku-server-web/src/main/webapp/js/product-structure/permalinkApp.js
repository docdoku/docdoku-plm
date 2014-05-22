var sceneManager,instancesManager;

// Global Namespace for the application
var App = {
    SceneOptions: {
        zoomSpeed: 1.2,
        rotateSpeed: 1.0,
        panSpeed: 0.3,
        cameraNear: 10,
        cameraFar: 5E5,
        defaultCameraPosition: {x: -1000, y: 800, z: 1100}
    }
};

define(["dmu/SceneManager","dmu/LoaderManager"],
function(SceneManager,LoaderManager){

    function PermalinkApp(fileName, width, height){
        var loaderManager = new LoaderManager({progressBar: true});
        sceneManager = new SceneManager();
        sceneManager.initRenderer = function() {
            this.renderer = new THREE.WebGLRenderer({alpha: true});
            this.renderer.setSize(width, height);
            this.$container.append(this.renderer.domElement);
        };
        sceneManager.init();
        var texturePath = fileName.substring(0, fileName.lastIndexOf('/'));
        loaderManager.parseFile(fileName,texturePath,{
            success: function(geometry, material){
                THREE.GeometryUtils.center(geometry);
                sceneManager.scene.add(new THREE.Mesh(geometry, material));
                //var boundingBox = mesh.geometry.boundingBox;
                //var cog = new THREE.Vector3((boundingBox.max.x-boundingBox.min.x)/2,(boundingBox.max.y-boundingBox.min.y)/2,(boundingBox.max.z-boundingBox.min.z)/2);
                //var radius = Math.max(boundingBox.size().x,boundingBox.size().y,boundingBox.size().z);
                //sceneManager.placeCamera(cog,radius);                                                                 //Todo place camera on the mesh
            }
        });
    }

    return PermalinkApp;

});
