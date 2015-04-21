/*global _,define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var linkedDocument = Backbone.Model.extend({
        initialize: function () {
            _.bindAll(this);
        },

        getWorkspace: function () {
            return this.get('workspaceId');
        },

        getReference: function () {
            return this.getDocKey() + '-' + this.getIteration();
        },

        getId: function () {
            return this.get('id');
        },

        getIteration: function () {
            return this.get('iteration');
        },

        getDocumentMasterId: function () {
            return this.get('documentMasterId');
        },

        getDocumentRevisionTitle: function () {
            return this.get('documentTitle');
        },

        getDocumentRevisionVersion: function () {
            return this.get('documentRevisionVersion');
        },

        // TODO use this only for display
        getDocKey: function () {
            if (this.getDocumentRevisionTitle()) {
                return this.getDocumentRevisionTitle() + ' < ' + this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion() + ' >';
            }
            return '< ' + this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion() + ' >';
        },

        getDocumentMasterPermalink: function () {
            return encodeURI(
                    window.location.origin +
                    App.config.contextPath +
                    '/documents/' +
                    this.getWorkspace() +
                    '/' +
                    this.getDocumentMasterId() +
                    '/' +
                    this.getDocumentRevisionVersion()
            );
        },

        setDocumentLinkComment:function(comment){
            this.set('commentLink', comment);
        },

        getDocumentLinkComment:function(comment){
            return this.get('commentLink');
        }
    });
    return linkedDocument;
});
