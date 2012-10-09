define([
    "models/marker"
], function (
    Marker
) {

    var MarkerCollection = Backbone.Collection.extend({

        model: Marker,

        url: function() {
            return this.urlLayer + "/markers";
        },

        initialize: function(models, options) {
            this.urlLayer = options.urlLayer;
        },

        onScene: function() {
            return this.where({onScene: true});
        },

        notOnScene: function() {
            return this.where({onScene: false});
        }

    });

    return MarkerCollection;

});
