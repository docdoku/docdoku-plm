/*global _,$,define,App*/
define([
    'common-objects/models/user',
    'common-objects/models/workspace'

], function (User, Workspace) {

    'use strict';


    var ContextResolver = function () {
    };


    $.ajaxSetup({
        beforeSend: function(xhr) {
            if(localStorage.jwt){
                xhr.setRequestHeader('Authorization', 'Bearer '+ localStorage.jwt);
            }
        },
        statusCode: {
            401: function(){
                delete localStorage.jwt;
                if(App.config.needAuthentication){
                    window.location.href = App.config.contextPath + '/?denied=true&originURL=' + encodeURIComponent(window.location.pathname + window.location.hash);
                }
            }
        }
    });

    function onError(res) {
        return res;
    }

    ContextResolver.prototype.resolveServerProperties = function(){
        return $.getJSON('../webapp.properties.json').then(function (properties) {
            App.config.contextPath = properties.contextRoot;
        },onError);
    };

    ContextResolver.prototype.resolveAccount = function(){
        return User.getAccount().then(function(account){
            App.config.connected = true;
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

    return new ContextResolver();
});
