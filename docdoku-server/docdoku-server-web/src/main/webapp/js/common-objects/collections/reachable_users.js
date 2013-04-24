define([
    "common-objects/models/user"
], function (
    User
    ) {
    var Users = Backbone.Collection.extend({
        model: User,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/users/reachable"
    });

    return Users;
});
