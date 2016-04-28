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

    var errorStatusHandlers = {
        403 : function(){
            var forbiddenView = new ForbiddenView().render();
            document.body.appendChild(forbiddenView.el);
            forbiddenView.openModal();
        },
        404 : function(){
            window.location.href = App.config.contextPath + '/404';
        }
    };

    function onError(res) {
        var status = res.status;
        if(typeof errorStatusHandlers[status] === 'function'){
            errorStatusHandlers[status]();
        }
        else{
            window.location.href = App.config.contextPath + '/faces/admin/workspace/workspacesMenu.xhtml?error='+res.status;
        }
    }

    ContextResolver.prototype.resolveServerProperties = function(){
        return $.getJSON('../server.properties.json').then(function (properties) {
            App.config.contextPath = properties.contextRoot;
        },onError);
    };

    ContextResolver.prototype.resolveAccount = function(){
        return User.getAccount().then(function(account){

            App.config.account = account;
            App.config.login = account.login;
            App.config.userName = account.name;
            App.config.timeZone = account.timeZone;
            App.config.admin = account.admin;

            if(window.localStorage.locale === 'unset'){
                window.localStorage.locale = account.language || 'en';
                window.location.reload();
            }else{
                window.localStorage.locale = account.language || 'en';
            }

            return account;
        },onError);
    };

    ContextResolver.prototype.resolveWorkspaces = function () {
        return Workspace.getWorkspaces().then(function (workspaces) {
            App.config.workspaces = workspaces;
            App.config.workspaceAdmin = _.findWhere(App.config.workspaces.administratedWorkspaces,{id:App.config.workspaceId}) !== undefined;
            App.config.workspaces.nonAdministratedWorkspaces = _.reject(App.config.workspaces.allWorkspaces,function(workspace){
                return _.contains(_.pluck(App.config.workspaces.administratedWorkspaces,'id'),workspace.id);
            });
            return workspaces;

        },onError);
    };

    ContextResolver.prototype.resolveGroups = function () {
        return User.getGroups(App.config.workspaceId)
            .then(function(groups){
                App.config.groups = groups;
            },onError);
    };

    ContextResolver.prototype.resolveUser = function () {
        return User.whoami(App.config.workspaceId)
            .then(function(user){
                App.config.user = user;
            },onError);
    };

    ContextResolver.prototype.resolveUser = function () {
        return User.whoami(App.config.workspaceId, onError);
    };

    return new ContextResolver();
});
