var sceneManager;

define(["models/part_iteration_visualization","dmu/SceneManager"], function (PartIteration,SceneManager) {

    var FrameAppView = Backbone.View.extend({

        el: $("#product-content"),

        initialize: function() {

            window.config_spec = "latest";

            SceneManager.prototype.initIFrameScene = function() {

                this.init();

                if (!_.isUndefined(SCENE_INIT.pathForIFrame)) {
                    var self = this;
                    var instancesUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/instances?configSpec=" + window.config_spec + "&path=" + SCENE_INIT.pathForIFrame;
                    $.getJSON(instancesUrl, function(instances) {
                        _.each(instances, function(instanceRaw) {

                            //do something only if this instance is not on scene
                            if (!self.isOnScene(instanceRaw.id)) {

                                //if we deal with this partIteration for the fist time, we need to create it
                                if (!self.hasPartIteration(instanceRaw.partIterationId)) {
                                    self.addPartIteration(new self.PartIteration(instanceRaw));
                                }

                                var partIteration = self.getPartIteration(instanceRaw.partIterationId);

                                //finally we create the instance and add it to the scene
                                self.addInstanceOnScene(new Instance(
                                    instanceRaw.id,
                                    partIteration,
                                    instanceRaw.tx,
                                    instanceRaw.ty,
                                    instanceRaw.tz,
                                    instanceRaw.rx,
                                    instanceRaw.ry,
                                    instanceRaw.rz
                                ));
                            }

                        });
                    });
                }
            }

            sceneManager = new SceneManager({
                PartIteration: PartIteration
            });
            sceneManager.initIFrameScene();
        }

    });

    return FrameAppView;

});