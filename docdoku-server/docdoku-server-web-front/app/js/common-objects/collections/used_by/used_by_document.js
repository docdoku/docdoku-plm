/*global define*/
define([
    'backbone',
    'common-objects/models/document/document_revision'
], function (Backbone, DocumentRevision) {
    'use strict';
    var UsedByDocumentList = Backbone.Collection.extend({

        model: DocumentRevision,

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
