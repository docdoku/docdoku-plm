/*global define*/
define([
    'backbone',
    'common-objects/models/part'
], function (Backbone, Part) {
    'use strict';
    var PartList = Backbone.Collection.extend({

        model: Part,

        className: 'PartList',

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
