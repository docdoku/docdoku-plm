/*global _,define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var Role = Backbone.Model.extend({

        initialize: function () {
            this.className = 'Role';
            _.bindAll(this);
        },

        getName: function () {
            return this.get('name');
        },

        getDefaultAssignedUsers: function () {
            return this.get('defaultAssignedUsers');
        },

        setDefaultAssignedUsers: function (users) {
            return this.set('defaultAssignedUsers',users);
        },

        getDefaultAssignedGroups: function () {
            return this.get('defaultAssignedGroups');
        },

        setDefaultAssignedGroups: function (groups) {
            return this.set('defaultAssignedGroups',groups);
        }
    });
    return Role;
});
