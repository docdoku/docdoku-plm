var sceneManager;

define(["models/part_iteration"], function (PartIteration) {

    var FrameAppView = Backbone.View.extend({

        el: $("#workspace"),

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