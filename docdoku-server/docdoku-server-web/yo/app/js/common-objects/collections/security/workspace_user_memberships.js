/*global define*/
define([
    'backbone',
    "common-objects/models/security/workspace_user_membership"
], function (Backbone, WorkspaceUserMembership) {

    var WorkspaceUserMemberships = Backbone.Collection.extend({

        model: WorkspaceUserMembership,

        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/memberships/users";
        }

    });

    return WorkspaceUserMemberships;
});