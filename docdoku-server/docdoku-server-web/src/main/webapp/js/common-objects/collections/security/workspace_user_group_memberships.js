define([
    "common-objects/models/security/workspace_user_group_membership"
], function (WorkspaceUserGroupMembership) {

    var WorkspaceUserGroupMemberships = Backbone.Collection.extend({

        model: WorkspaceUserGroupMembership,

        url:function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/memberships/usergroups"
        }

    });

    return WorkspaceUserGroupMemberships;
});