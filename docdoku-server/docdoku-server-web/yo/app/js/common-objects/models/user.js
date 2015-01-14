/*global $,define,App*/
define(['backbone'],
function (Backbone) {
	'use strict';
    var UserModel = Backbone.Model.extend({
        getLogin: function () {
            return this.get('login');
        }
    });

    UserModel.whoami = function (workspaceId, success, error) {

        $.getJSON(App.config.contextPath + '/api/workspaces/' + workspaceId + '/users/me').success(function (user) {

            UserModel.getAccount(function (account) {

                user.timeZone = account.timeZone;

                UserModel.getGroups(workspaceId, function (groups) {

                    success(user, groups);

                }, error);

            }, error);

        }).error(error);
    };

    UserModel.getGroups = function (workspaceId, success, error) {
        $.getJSON(App.config.contextPath + '/api/workspaces/' + workspaceId + '/memberships/usergroups/me').success(success).error(error);
    };

    UserModel.getAccount = function (success, error) {
        $.getJSON(App.config.contextPath + '/api/accounts/me').success(success).error(error);
    };

    return UserModel;
});
