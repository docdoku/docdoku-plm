/*global _,define*/
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

        getDocumentMasterId: function () {
            return this.get('documentMasterId');
        },

        getDocumentRevisionTitle: function () {
            return this.get('title');
        },

        getDocumentRevisionVersion: function () {
            return this.get('version');
        },

        getDocKey: function () {
            return this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion();
        },

        getDisplayDocKey: function () {
            if (this.getDocumentRevisionTitle()) {
                return this.getDocumentRevisionTitle() + ' < ' + this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion() + ' >';
            }
            return '< ' + this.getDocumentMasterId() + '-' + this.getDocumentRevisionVersion() + ' >';
        },

        setDocumentLinkComment:function(comment){
            this.set('commentLink', comment);
        },

        getDocumentLinkComment:function(){
            return this.get('commentLink');
        }
    });
    return linkedDocument;
});
