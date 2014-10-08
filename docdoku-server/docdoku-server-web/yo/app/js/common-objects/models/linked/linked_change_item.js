/*global _,define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var linkedChangeItems = Backbone.Model.extend({

        initialize: function () {
			_.bindAll(this);
        },

        getId: function () {
            return this.get('id');
        },

        getName: function () {
            return this.get('name');
        },

        getPriority: function () {
            return this.get('priority');
        },

        getCategory: function () {
            return this.get('category');
        },

        getAffectedDocuments: function () {
            return this.get('affectedDocuments');
        },

        getAffectedParts: function () {
            return this.get('affectedParts');
        }
    });
    return linkedChangeItems;
});
