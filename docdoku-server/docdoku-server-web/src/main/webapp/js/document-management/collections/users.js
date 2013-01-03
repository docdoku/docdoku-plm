define([
    "models/user"
], function (
    User
    ) {
    var Users = Backbone.Collection.extend({
        model: User,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/users"
    });

    return Users;
});
