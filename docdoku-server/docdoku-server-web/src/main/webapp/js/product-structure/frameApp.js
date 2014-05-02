var sceneManager, instancesManager;

// Global Namespace for the application
var App = {
    WorkerManagedValues: {
        maxInstances: 200,
        maxAngle: Math.PI / 4,
        maxDist: 100000,
        minProjectedSize: 0.000001,
        distanceRating: 0.7,
        angleRating: 0.7,
        volRating: 1.0
    },

    SceneOptions: {
        postProcessing: false,
        grid: false,
        ground: false,
        debugColors: false,
        skeleton: true,
        zoomSpeed: 2,
        rotateSpeed: 1,
        panSpeed: 2,
        cameraNear: 10,
        cameraFar: 5E5,
        showLayers: true,
        defaultCameraPosition: {x: -21262.730734573677, y: 13214.484586955678, z: 9104.792300874204},
        defaultTargetPosition: {x: -20486.55024906156, y: 12882.929921870797, z: 8910.845509277657}
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