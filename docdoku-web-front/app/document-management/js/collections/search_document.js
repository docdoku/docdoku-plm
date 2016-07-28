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
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/documents/search?' + this.query;
        }
    });

    return SearchDocumentList;
});
