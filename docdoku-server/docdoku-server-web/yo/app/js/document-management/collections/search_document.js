/*global define*/
define([
    'backbone',
    'models/document'
], function (Backbone,Document) {
    var SearchDocumentList = Backbone.Collection.extend({

        model: Document,

        className: 'SearchDocumentList',

        setQuery: function (query) {
            this.query = query;
            return this;
        },

        url: function () {
            var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/search';
            return baseUrl + '/' + this.query + '/documents?configSpec='+App.config.configSpec;
        }
    });

    return SearchDocumentList;
});
