define([
    "models/layer"
], function (
    Layer
) {

    var LayerCollection = Backbone.Collection.extend({
        model: Layer
    });

    return LayerCollection;

});
