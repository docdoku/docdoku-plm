var sceneManager, instancesManager;

define(["models/part_iteration_visualization","dmu/SceneManager","dmu/InstancesManager"], function (PartIteration,SceneManager,InstancesManager) {

    var FrameAppView = Backbone.View.extend({

        el: $("#product-content"),

        initialize: function() {

            window.config_spec = "latest";

            SceneManager.prototype.initIFrameScene = function() {
                this.init();
                if (!_.isUndefined(SCENE_INIT.pathForIFrame)) {
                    var instancesUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/instances?configSpec=" + window.config_spec + "&path=" + SCENE_INIT.pathForIFrame;
                    var Component = function() {
                        this.getInstancesUrl=function(){
                            return instancesUrl;
                        };
                    };
                    instancesManager.loadFromTree(new Component());
                }
            }

            instancesManager = new InstancesManager();
            instancesManager.init();

            sceneManager = new SceneManager({
                PartIteration: PartIteration
            });
            sceneManager.initIFrameScene();
        }

    });

    return FrameAppView;

});