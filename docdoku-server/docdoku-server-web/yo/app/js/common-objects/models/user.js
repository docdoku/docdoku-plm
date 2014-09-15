/*global define*/
define(['backbone'],
    function (Backbone) {
        var UserModel = Backbone.Model.extend({
            getLogin: function () {
                return this.get("login");
            }
        });

        UserModel.whoami = function (workspaceId, success, error) {
            $.getJSON(APP_CONFIG.contextPath + '/api/workspaces/' + workspaceId + '/users/me').success(function (user) {
                UserModel.getGroups(workspaceId, function (groups) {
                    success(user, groups);
                }, error);
            }).error(error);
        };

        UserModel.getGroups = function (workspaceId, success, error) {
            $.getJSON(APP_CONFIG.contextPath + '/api/workspaces/' + workspaceId + '/memberships/usergroups/me').success(success).error(error);
        };

        return UserModel;
    });
