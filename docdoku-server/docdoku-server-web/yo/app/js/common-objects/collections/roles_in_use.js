/*global define*/
define([
    'backbone',
    "common-objects/models/role"
], function (Backbone, Role) {
    var RoleInUseList = Backbone.Collection.extend({
        model: Role,

        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/roles/inuse";
        }

    });

    return RoleInUseList;
});
