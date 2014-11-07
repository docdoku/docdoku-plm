/*global define*/
define([
    "backbone",
    "models/part"
], function (Backbone, Part) {
    var PartList = Backbone.Collection.extend({

        model: Part,

        className: "PartList",

        initialize: function () {
            this.filterUrl = undefined;
        },

        setFilterUrl: function (url) {
            this.filterUrl = url;
        },

        url: function () {
            return this.filterUrl;
        }

    });

    return PartList;
});
