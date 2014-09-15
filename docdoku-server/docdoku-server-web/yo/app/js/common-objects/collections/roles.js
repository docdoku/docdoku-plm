/*global define*/
define([
    'backbone',
    "common-objects/models/role"
], function (Backbone, Role) {
    var RoleList = Backbone.Collection.extend({
        model: Role,

        className: "RoleList",

        comparator: function (a, b) {
            // sort roles by name
            var nameA = a.get("name");
            var nameB = b.get("name");

            if (nameA == nameB) {
                return 0;
            }
            return (nameA < nameB) ? -1 : 1;
        },


        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/roles/";
        }

    });

    return RoleList;
});
