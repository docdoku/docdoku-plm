/*global define*/
define([
    "backbone",
    "models/document"
], function (Backbone,Document) {
    var SearchDocumentList = Backbone.Collection.extend({

        model: Document,

        className: "SearchDocumentList",

        setQuery: function (query) {
            this.query = query;
            return this;
        },

        url: function () {
            var baseUrl = APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/search";
            return baseUrl + "/" + this.query + "/documents";
        }
    });

    return SearchDocumentList;
});
