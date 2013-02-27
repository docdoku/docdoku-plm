var sceneManager;

define(["models/part_iteration_visualization","SceneManager"], function (PartIteration,SceneManager) {

    var FrameAppView = Backbone.View.extend({

        el: $("#product-content"),

        initialize: function() {
            var self = this;
            sceneManager = new SceneManager({
                PartIteration: PartIteration
            });
            sceneManager.init();
        }

    });

    return FrameAppView;

});