/*global define*/
define([
    'backbone',
    'models/document'
], function (Backbone, Document) {
    'use strict';
    var UsedByDocumentList = Backbone.Collection.extend({

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

    UsedByDocumentList.className = 'UsedByDocumentList';

    return UsedByDocumentList;
});
