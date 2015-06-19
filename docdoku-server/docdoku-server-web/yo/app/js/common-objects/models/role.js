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

        getMappedUser: function () {
            return this.get('defaultAssignee');
        },

        getMappedUserLogin: function () {
            if (this.getMappedUser()) {
                return this.getMappedUser().login;
            }
            return '';
        }
    });
    return Role;
});
