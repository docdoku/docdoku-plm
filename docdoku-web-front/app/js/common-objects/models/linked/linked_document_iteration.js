/*global _,define*/
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

        getTitle: function () {
            return this.get('title');
        },

        getVersion: function () {
            return this.get('version');
        },

        getIteration: function () {
            return this.get('iteration');
        },

        getDocumentLinkComment:function(){
            return this.get('commentLink');
        },

        getDocKey: function () {
            return this.getDocumentMasterId() + '-' + this.getVersion() + '-' + this.getIteration();
        },

        getDisplayDocKey: function () {
            if (this.getTitle()) {
                return this.getTitle() + ' < ' + this.getDocumentMasterId() + '-' + this.getVersion() + '-' + this.getIteration() + ' >';
            }
            return '< ' + this.getDocumentMasterId() + '-' + this.getVersion() + '-' + this.getIteration() + ' >';
        }

    });
    return linkedDocumentIteration;
});
