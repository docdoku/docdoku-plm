/*global SCENE_INIT, APP_CONFIG*/
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
        grid: false,
        skeleton: true,
        zoomSpeed: 1.2,
        rotateSpeed: 1.0,
        panSpeed: 0.3,
        cameraNear: 1,
        cameraFar: 5E4,
        defaultCameraPosition: {x: -1000, y: 800, z: 1100},
        defaultTargetPosition: {x: 0, y: 0, z: 0}
    }

};

define(['dmu/SceneManager','dmu/InstancesManager'],
    function (SceneManager,InstancesManager) {

        var FrameAppView = Backbone.View.extend({

        el: $('#product-content'),

            initialize: function() {

                APP_CONFIG.configSpec = "latest";
                try{
                    App.frameApp = true;
                    App.instancesManager = new InstancesManager();
                    App.sceneManager = new SceneManager();
                    App.sceneManager.init();
                }catch(ex){
                    console.log("Got exception in dmu");
                    this.onNoWebGLSupport();
                }
                if (!_.isUndefined(SCENE_INIT.pathForIFrame)) {
                    App.instancesManager.loadQueue.push({"process":"load","path":[SCENE_INIT.pathForIFrame]});
                }
            }

        });

        return FrameAppView;
    });