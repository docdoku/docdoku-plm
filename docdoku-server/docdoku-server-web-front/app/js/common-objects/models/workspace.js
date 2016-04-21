/*global $,define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var Workspace = Backbone.Model.extend({
        initialize: function () {
            this.className = 'Workspace';
        }
    });
    Workspace.getWorkspaces = function (success, error) {
        $.getJSON(App.config.contextPath + '/api/workspaces', success, error);
    };
    return Workspace;
});
