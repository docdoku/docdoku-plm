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

    return Admin;
});
