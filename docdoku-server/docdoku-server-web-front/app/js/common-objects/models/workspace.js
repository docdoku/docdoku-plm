/*global $,define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var Workspace = Backbone.Model.extend({
        initialize: function () {
            this.className = 'Workspace';
        }
    });

    Workspace.getWorkspaces = function () {
        return $.getJSON(App.config.contextPath + '/api/workspaces');
    };

    Workspace.createWorkspace = function (workspace) {
        return $.ajax({
            type: 'POST',
            url: App.config.contextPath + '/api/workspaces',
            data: JSON.stringify(workspace),
            contentType: 'application/json; charset=utf-8'
        });
    };

    return Workspace;
});
