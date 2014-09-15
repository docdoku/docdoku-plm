/*global define*/
define([
    'common-objects/models/user',
    'common-objects/models/workspace'
], function (User, Workspace) {

    var ContextResolver = function () {
    };

    ContextResolver.prototype.resolve = function (success) {
        $.getJSON('../server.properties.json').success(function (data) {
            APP_CONFIG.contextPath = data['contextRoot'];
            User.whoami(APP_CONFIG.workspaceId, function (user, groups) {
                Workspace.getWorkspaces(function (workspaces) {
                    APP_CONFIG.login = user.login;
                    APP_CONFIG.userName = user.name;
                    APP_CONFIG.groups = groups;
                    APP_CONFIG.workspaces = workspaces;
                    APP_CONFIG.workspaceAdmin = _.select(APP_CONFIG.workspaces.administratedWorkspaces, function (workspace) {
                        return workspace.id === APP_CONFIG.workspaceId
                    }).length === 1;
                    localStorage.setItem('locale', user.language || 'en');
                    success();
                });
            });
        });
    };

    return new ContextResolver;

});