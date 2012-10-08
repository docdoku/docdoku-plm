define([
    "models/layer"
], function (
    Layer
) {

    var LayerCollection = Backbone.Collection.extend({

        model: Layer,

        localStorage: new Store("plm:layers"),

        setAllShown: function(allShown) {
            this.each(function(layer) {
                layer.setShown(allShown);
            });
        },

        areInEditingMarkers: function() {
            return this.where({editingMarkers: true});
        },

        setAllEditingMarkers: function(editingMarkers) {
            _.each(this.areInEditingMarkers(), function(layer) {
                layer.setEditingMarkers(editingMarkers);
            }, this);
        }

    });

    return LayerCollection;

});
