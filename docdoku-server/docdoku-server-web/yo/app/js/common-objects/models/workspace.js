/*global define*/
define(['backbone'], function (Backbone) {
    var Workspace = Backbone.Model.extend({
        initialize: function () {
            this.className = "Workspace";
        }
    });
    Workspace.getWorkspaces = function (success, error) {
        $.getJSON(APP_CONFIG.contextPath + '/api/workspaces', success, error);
    };
    return Workspace;
});
