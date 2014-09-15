/*global define,App*/
define([ "backbone", "mustache", "text!templates/control_markers.html"], function (Backbone, Mustache, template) {

    var ControlMarkersView = Backbone.View.extend({

        className: "side_control_group",

        events: {
            "click button#markerZoomLess": "markerZoomLess",
            "click button#markerState": "markerState",
            "click button#markerZoomMore": "markerZoomMore"
        },

        initialize: function () {
        },

        markerZoomLess: function () {
            if (App.sceneManager.layerManager.markerScale.x > 0.2) {
                App.sceneManager.layerManager.markerScale.addScalar(-0.1);
                App.sceneManager.layerManager.rescaleMarkers();
            }
        },
        markerState: function () {
            App.sceneManager.layerManager.changeMarkerState();
        },

        markerZoomMore: function () {
            App.sceneManager.layerManager.markerScale.addScalar(0.1);
            App.sceneManager.layerManager.rescaleMarkers();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}));
            return this;
        }


    });

    return ControlMarkersView;

});