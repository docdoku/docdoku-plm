/*global define,App*/
define([
    'backbone',
    'common-objects/models/document/document_revision'
], function (Backbone, DocumentRevision) {
    'use strict';
    var SearchDocumentList = Backbone.Collection.extend({
        model: DocumentRevision,

        className: 'SearchDocumentList',

        setQuery: function (query) {
            this.query = query;
            return this;
        },

        url: function () {
            var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId;
            return baseUrl +  '/documents/search?configSpec='+App.config.documentConfigSpec+ '&'+this.query ;
        }
    });

    return SearchDocumentList;
});
