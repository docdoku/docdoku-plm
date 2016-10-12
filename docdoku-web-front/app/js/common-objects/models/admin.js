/*global $,define,App*/
define(['backbone'], function (Backbone) {
    'use strict';
    var Admin = Backbone.Model.extend({
        initialize: function () {
            this.className = 'Admin';
        }
    });

    Admin.getDiskSpaceUsageStats = function () {
        return $.getJSON(App.config.contextPath + '/api/admin/disk-usage-stats');
    };
    Admin.getUsersStats = function () {
        return $.getJSON(App.config.contextPath + '/api/admin/users-stats');
    };
    Admin.getDocumentsStats = function () {
        return $.getJSON(App.config.contextPath + '/api/admin/documents-stats');
    };
    Admin.getProductsStats = function () {
        return $.getJSON(App.config.contextPath + '/api/admin/products-stats');
    };
    Admin.getPartsStats = function () {
        return $.getJSON(App.config.contextPath + '/api/admin/parts-stats');
    };

    Admin.indexWorkspace = function (workspaceId) {
        return $.ajax({
            type: 'PUT',
            url: App.config.contextPath +  '/api/admin/index/'+workspaceId
        });
    };

    Admin.enableWorkspace = function (workspaceId, enabled) {
        return $.ajax({
            type: 'PUT',
            url: App.config.contextPath +  '/api/admin/workspace/' + workspaceId + '/enable?enabled='+enabled
        });
    };

    Admin.enableAccount = function (login, enabled) {
        return $.ajax({
            type: 'PUT',
            url: App.config.contextPath +  '/api/admin/account/' + login + '/enable?enabled='+enabled
        });
    };

    Admin.indexAllWorkspaces = function () {
        return $.ajax({
            type: 'PUT',
            url: App.config.contextPath +  '/api/admin/index-all'
        });
    };

    Admin.getPlatformOptions = function () {
        return $.getJSON(App.config.contextPath +  '/api/admin/platform-options');
    };

    Admin.getAccounts = function () {
        return $.getJSON(App.config.contextPath +  '/api/accounts');
    };

    Admin.setPlatformOptions = function (options) {
        return $.ajax({
            type: 'PUT',
            url: App.config.contextPath +  '/api/admin/platform-options',
            data: JSON.stringify(options),
            contentType: 'application/json'
        });
    };

    return Admin;
});
