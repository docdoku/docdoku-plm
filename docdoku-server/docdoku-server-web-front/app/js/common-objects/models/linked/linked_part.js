/*global _,define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var linkedPart = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },

        getWorkspace: function () {
            return this.get('workspaceId');
        },

        getReference: function () {
            return this.getPartKey() + '-' + this.getIteration();
        },

        getId: function () {
            return this.get('id');
        },

        getIteration: function () {
            return this.get('iteration');
        },

        getNumber: function () {
            return this.get('number');
        },

        getName: function () {
            return this.get('name');
        },

        getVersion: function () {
            return this.get('version');
        },

        getPartKey: function () {
            return this.getNumber() + '-' + this.getVersion();
        },

        getDisplayPartKey: function () {
            if (this.getName()) {
                return this.getName() + ' < ' + this.getNumber() + '-' + this.getVersion() + ' >';
            }
            return '< ' + this.getNumber() + '-' + this.getVersion() + ' >';
        },

        getPartMasterPermalink: function () {
            return encodeURI(
                    window.location.origin +
                    App.config.contextPath +
                    '/parts/#' +
                    this.getWorkspace() +
                    '/' +
                    this.getNumber() +
                    '/' +
                    this.getVersion()
            );
        }
    });
    return linkedPart;
});
