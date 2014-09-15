/*global define*/
define([
    'backbone',
    "common-objects/models/security/workspace_user_group_membership"
], function (Backbone, WorkspaceUserGroupMembership) {

    var WorkspaceUserGroupMemberships = Backbone.Collection.extend({

        model: WorkspaceUserGroupMembership,

        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/memberships/usergroups";
        }

    });

    return WorkspaceUserGroupMemberships;
});