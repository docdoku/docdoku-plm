/*global _,define,App*/
define([
    'backbone',
    'models/layer'
], function (Backbone, Layer) {
    'use strict';
    var LayerCollection = Backbone.Collection.extend({

        model: Layer,

        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/layers';
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
