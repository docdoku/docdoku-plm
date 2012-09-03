define(function() {

    var Marker = Backbone.Model.extend({

        getX: function() {
            return this.get('x');
        },

        getY: function() {
            return this.get('y');
        },

        getZ: function() {
            return this.get('z');
        },

        getTitle: function() {
            return this.get('title');
        },

        getDescription: function() {
            return this.get('description');
        }

    });

    return Marker;

});