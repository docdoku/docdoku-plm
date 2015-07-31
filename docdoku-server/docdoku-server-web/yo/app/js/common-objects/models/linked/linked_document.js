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

        getTitle: function () {
            return this.get('title');
        },

        getVersion: function () {
            return this.get('version');
        },

        getDocKey: function () {
            return this.getDocumentMasterId() + '-' + this.getVersion();
        },

        getDisplayDocKey: function () {
            if (this.getTitle()) {
                return this.getTitle() + ' < ' + this.getDocumentMasterId() + '-' + this.getVersion() + ' >';
            }
            return '< ' + this.getDocumentMasterId() + '-' + this.getVersion() + ' >';
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
