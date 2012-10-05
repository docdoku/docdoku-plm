define([
    "models/marker"
], function (
    Marker
) {

    var MarkerCollection = Backbone.Collection.extend({

        model: Marker,

        onScene: function() {
            return this.where({onScene: true});
        },

        notOnScene: function() {
            return this.where({onScene: false});
        }

    });

    return MarkerCollection;

});
