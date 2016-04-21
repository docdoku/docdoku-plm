/*global $,define,App*/
define(['backbone'],
function (Backbone) {
	'use strict';
    var UserModel = Backbone.Model.extend({
        getLogin: function () {
            return this.get('login');
        }
    });

    UserModel.whoami = function (workspaceId) {
        return $.getJSON(App.config.contextPath + '/api/workspaces/' + workspaceId + '/users/me');
    };

    UserModel.getGroups = function (workspaceId) {
        return $.getJSON(App.config.contextPath + '/api/workspaces/' + workspaceId + '/memberships/usergroups/me');
    };

    UserModel.getAccount = function () {
        return $.getJSON(App.config.contextPath + '/api/accounts/me');
    };

    return UserModel;
});
