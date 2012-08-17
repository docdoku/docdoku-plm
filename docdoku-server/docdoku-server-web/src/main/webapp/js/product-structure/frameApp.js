var sceneManager;

window.FrameAppView = Backbone.View.extend({

    el: $("#workspace"),

    initialize: function() {
        sceneManager = new SceneManager();
        var allParts = new PartCollection;
        allParts.fetch();
        sceneManager.init();
    }

});

$(document).ready(function() {
    window.App = new FrameAppView;
});