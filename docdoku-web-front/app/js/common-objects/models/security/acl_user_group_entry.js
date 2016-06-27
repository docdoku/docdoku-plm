/*global _,define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var UserAclEntry = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },

        key: function () {
            return this.get('groupId');
        },

        isForbidden: function () {
            return this.getPermission() === 'FORBIDDEN';
        },
        isReadOnly: function () {
            return this.getPermission() === 'READ_ONLY';
        },
        isFullAccess: function () {
            return this.getPermission() === 'FULL_ACCESS';
        },

        setPermission: function (permission) {
            this.set('permission', permission);
        },

        getPermission: function () {
            return this.get('permission');
        }

    });

    return UserAclEntry;
});