'use strict';
var sceneManager;

// Global Namespace for the application
var App = {
    debug: false,
    instancesManager : null,
    sceneManager : null,

    setDebug:function(state){
        App.debug = state;
        if(state){
            $('body').addClass('debug');
        }else{
            $('body').removeClass('debug');
        }
    },

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

define(['dmu/SceneManager','dmu/InstancesManager'],
    function (SceneManager,InstancesManager) {

    var FrameAppView = Backbone.View.extend({

        el: $('#product-content'),

        initialize: function() {

            window.configSpec = 'latest';

            App.instancesManager = new InstancesManager();
            App.sceneManager = new SceneManager();
            App.sceneManager.init();
            if (!_.isUndefined(SCENE_INIT.pathForIFrame)) {
                var instancesUrl = '/api/workspaces/' + APP_CONFIG.workspaceId + '/products/' + APP_CONFIG.productId + '/instances?configSpec=' + window.configSpec + '&path=' + SCENE_INIT.pathForIFrame;
                App.instancesManager.loadComponent({
                    getInstancesUrl:function (){
                        return instancesUrl;
                    }
                });
            }
        }

    });

    return FrameAppView;
});