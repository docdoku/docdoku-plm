/*global define*/
define([
    'backbone',
    "common-objects/models/part"
], function (Backbone, Part) {
    var PartList = Backbone.Collection.extend({

        model: Part,

        className: "PartList",

        setQuery: function (query) {
            this.query = query;
        },

        initialize: function () {
            this.urlBase = APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/parts/search/";
        },

        fetchPageCount: function () {
            return false;
        },

        hasSeveralPages: function () {
            return false;
        },

        url: function () {
            return this.urlBase + this.query;
        }

    });

    return PartList;
});
