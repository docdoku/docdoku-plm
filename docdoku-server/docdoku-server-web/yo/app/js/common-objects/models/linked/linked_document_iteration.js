/*global _,define,App*/
define(['backbone'], function (Backbone) {
    'use strict';
    var linkedDocumentIteration = Backbone.Model.extend({
        initialize: function () {
            _.bindAll(this);
        },

        getWorkspace: function () {
            return this.get('workspaceId');
        },

        getId: function () {
            return this.get('id');
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

        getIteration: function () {
            return this.get('iteration');
        },

        getDocumentLinkComment:function(){
            return this.get('commentLink');
        },

        getDocKey: function () {
            return this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion() + '-' + this.getIteration();
        },

        getDisplayDocKey: function () {
            if (this.getDocumentRevisionTitle()) {
                return this.getDocumentRevisionTitle() + ' < ' + this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion() + '-' + this.getIteration() + ' >';
            }
            return '< ' + this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion() + '-' + this.getIteration() + ' >';
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
    return linkedDocumentIteration;
});
