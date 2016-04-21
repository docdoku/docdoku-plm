/*global _,$,define,App*/
define([
    'common-objects/models/user',
    'common-objects/models/workspace',
    'common-objects/views/forbidden'
], function (User, Workspace, ForbiddenView) {

    'use strict';

    var ContextResolver = function () {
    };

    $.ajaxSetup({
        statusCode: {
            401: function(){
                window.location.href = App.config.contextPath + '/faces/login.xhtml?originURL=' + window.location.pathname + window.location.hash;
            }
        }
    });

    ContextResolver.prototype.resolveUser = function (success) {
        $.getJSON('../server.properties.json').success(function (properties) {

            App.config.contextPath = properties.contextRoot;

            User.whoami(App.config.workspaceId, function (user, groups) {
                Workspace.getWorkspaces(function (workspaces) {

                    App.config.login = user.login;
                    App.config.userName = user.name;
                    App.config.timeZone = user.timeZone;
                    App.config.groups = groups;
                    App.config.workspaces = workspaces;
                    App.config.workspaceAdmin = _.select(App.config.workspaces.administratedWorkspaces, function (workspace) {
                        return workspace.id === App.config.workspaceId;
                    }).length === 1;

                    if(window.localStorage.locale === 'unset'){
                        window.localStorage.locale = user.language || 'en';
                        window.location.reload();
                        return;
                    }else{
                        window.localStorage.locale = user.language || 'en';
                    }

                    success();
                });
            }, function (res) {

                // Connected but no access to given workspace
                if(res.status === 403){
                    var forbiddenView = new ForbiddenView().render();
                    document.body.appendChild(forbiddenView.el);
                    forbiddenView.openModal();
                }
                // Connected but the workspace doesn't exist
                else if(res.status === 404){
                    window.location.href = App.config.contextPath + '/faces/admin/workspace/workspacesMenu.xhtml';
                }
                // However, for dev purposes, if we catch an error, we just reset the url. Should happen only in dev env.
                else{
                    window.location.href = App.config.contextPath + '/faces/admin/workspace/workspacesMenu.xhtml';
                }
            });
        });
    };

    return new ContextResolver();
});
