/*global define*/
define(['backbone'], function (Backbone) {

    var Admin = Backbone.Model.extend({

        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/users/admin";
        },

        getLogin: function () {
            return this.get("login");
        }

    });

    return Admin;

});