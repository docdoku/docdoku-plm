define([
    "common-objects/models/security/workspace_user_membership"
], function (WorkspaceUserMembership) {

    var WorkspaceUserMemberships = Backbone.Collection.extend({

        model: WorkspaceUserMembership,

        url:function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/memberships/users";
        }

    });

    return WorkspaceUserMemberships;
});