/*global define,App*/
define([
    "backbone",
    "models/layer"
], function (Backbone, Layer) {

    var LayerCollection = Backbone.Collection.extend({

        model: Layer,

        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/layers"
        },

        setAllShown: function (allShown) {
            this.each(function (layer) {
                layer.setShown(allShown);
            });
        },

        areInEditingMarkers: function () {
            return this.where({editingMarkers: true});
        },

        setAllEditingMarkers: function (editingMarkers) {
            _.each(this.areInEditingMarkers(), function (layer) {
                layer.setEditingMarkers(editingMarkers);
            }, this);
        },

        onEmpty: function () {
            App.sceneManager.stopMarkerCreationMode();
        }

    });

    return LayerCollection;

});
