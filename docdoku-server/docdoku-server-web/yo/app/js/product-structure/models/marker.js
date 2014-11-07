/*global define*/
define(['backbone'], function (Backbone) {

    var Marker = Backbone.Model.extend({

        toJSON: function () {
            return _.pick(this.attributes, 'id', 'title', 'description', 'x', 'y', 'z');
        },

        getX: function () {
            return this.get('x');
        },

        getY: function () {
            return this.get('y');
        },

        getZ: function () {
            return this.get('z');
        },

        getTitle: function () {
            return this.get('title');
        },

        getDescription: function () {
            return this.get('description');
        }

    });

    return Marker;

});