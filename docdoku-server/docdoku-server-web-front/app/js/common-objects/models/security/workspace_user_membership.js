/*global _,define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var WorkspaceUserMembership = Backbone.Model.extend({

        initialize: function () {
            var permission = this.isReadOnly() ? 'READ_ONLY' : 'FULL_ACCESS';
            this.setPermission(permission);
            _.bindAll(this);
        },

        key: function () {
            return this.getUserLogin();
        },

        name: function () {
            return this.getUserName();
        },

        getUserLogin: function () {
            return this.getUser().login;
        },

        getUserName: function () {
            return this.getUser().name;
        },

        getWorkspaceId: function () {
            return this.get('workspaceId');
        },

        getUser: function () {
            return this.get('member');
        },

        isReadOnly: function () {
            return this.get('readOnly');
        },

        setPermission: function (permission) {
            this.set('permission', permission);
        },

        getPermission: function () {
            return this.get('permission');
        }
    });

    return WorkspaceUserMembership;
});