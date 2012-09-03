define([
    "models/marker"
], function (
    Marker
) {

    var MarkerCollection = Backbone.Collection.extend({
        model: Marker
    });

    return MarkerCollection;

});
