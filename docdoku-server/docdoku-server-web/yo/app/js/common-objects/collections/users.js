/*global define*/
define([
    'backbone',
    "common-objects/models/user"
], function (Backbone, User) {
    var Users = Backbone.Collection.extend({
        model: User,
        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/users";
        }
    });

    return Users;
});
