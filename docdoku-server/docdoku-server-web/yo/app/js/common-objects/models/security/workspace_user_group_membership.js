/*global _,define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var WorkspaceUserGroupMembership = Backbone.Model.extend({

        initialize: function () {
            var permission = this.isReadOnly() ? 'READ_ONLY' : 'FULL_ACCESS';
            this.setPermission(permission);
            _.bindAll(this);
        },

        key: function () {
            return this.getGroupId();
        },

        name: function () {
            return this.key();
        },

        getWorkspaceId: function () {
            return this.get('workspaceId');
        },

        getGroupId: function () {
            return this.get('memberId');
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

    return WorkspaceUserGroupMembership;
});