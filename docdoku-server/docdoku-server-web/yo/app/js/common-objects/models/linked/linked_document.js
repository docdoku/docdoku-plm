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
            return  this.get('documentMasterId');
        },

        getDocumentRevisionVersion: function () {
            return  this.get('documentRevisionVersion');
        },

        getDocKey: function () {
            return  this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion();
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
        }
    });
    return linkedDocument;
});