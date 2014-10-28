/*global define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var Admin = Backbone.Model.extend({
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/users/admin';
        },

        getLogin: function () {
            return this.get('login');
        }

    });

    return Admin;

});