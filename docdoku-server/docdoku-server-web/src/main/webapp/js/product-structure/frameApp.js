var sceneManager, instancesManager;

// Global Namespace for the application
var App = {
    WorkerManagedValues: {
        maxInstances: 200,
        maxAngle: Math.PI / 4,
        maxDist: 100000,
        minProjectedSize: 0.000001,
        distanceRating: 0.7,
        angleRating: 0.6,
        volRating: 0.7
    },

    SceneOptions: {
        postProcessing: false,
        grid: false,
        zoomSpeed: 2,
        rotateSpeed: 1,
        panSpeed: 2,
        cameraNear: 10,
        cameraFar: 5E5,
        defaultCameraPosition: {x: -1000, y: 800, z: 1100}
    }

};

define(["dmu/SceneManager","dmu/InstancesManager"],
    function (SceneManager,InstancesManager) {

    var FrameAppView = Backbone.View.extend({

        el: $("#product-content"),

        initialize: function() {

            window.config_spec = "latest";

            instancesManager = new InstancesManager();
            sceneManager = new SceneManager();
            sceneManager.init();
            if (!_.isUndefined(SCENE_INIT.pathForIFrame)) {
                var instancesUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/instances?configSpec=" + window.config_spec + "&path=" + SCENE_INIT.pathForIFrame;
                instancesManager.start();
                instancesManager.loadFromTree({
                    getInstancesUrl:function (){
                        return instancesUrl;
                    }
                });
            }
        }

    });

    return FrameAppView;
});