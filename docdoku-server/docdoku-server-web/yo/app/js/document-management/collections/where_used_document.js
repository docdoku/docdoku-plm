/*global define,App*/
define([
    'backbone',
    'models/document'
], function (Backbone, Document) {
    'use strict';
    var WhereUsedDocumentList = Backbone.Collection.extend({

        model: Document,

        setLinkedDocumentIterationId: function (linkedDocumentIterationId) {
            this.linkedDocumentIterationId = linkedDocumentIterationId;
        },

        setLinkedDocument: function (linkedDocument) {
            this.linkedDocument = linkedDocument;
        },

        url: function () {
            return this.linkedDocument.baseUrl() + '/' + this.linkedDocumentIterationId + '/inverse-document-link';
        }

    });

    WhereUsedDocumentList.className = 'WhereUsedDocumentList';

    return WhereUsedDocumentList;
});
