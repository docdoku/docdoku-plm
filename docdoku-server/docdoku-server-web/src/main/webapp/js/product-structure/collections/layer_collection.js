define([
    "models/layer"
], function (
    Layer
) {

    var LayerCollection = Backbone.Collection.extend({

        model: Layer,

        localStorage: new Store("plm:layers"),

        toggleAllShown: function(allShown) {
            this.each(function(layer) {
                layer.setShown(allShown);
            });
        }

    });

    return LayerCollection;

});
