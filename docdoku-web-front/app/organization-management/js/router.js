/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            '': 'organizationHome',
            'create': 'organizationCreate',
            'edit': 'organizationEdit',
            'members': 'organizationMembers'
        },

        refresh:function() {
            App.appView.render();
            App.headerView.render();
        },

        organizationHome:function() {
            this.refresh();
            App.appView.organizationManagementHome();
        },

        organizationCreate:function() {
            this.refresh();
            App.appView.organizationCreate();
        },

        organizationEdit:function() {
            this.refresh();
            App.appView.organizationEdit();
        },

        organizationMembers:function() {
            this.refresh();
            App.appView.organizationMembers();
        }
    });

    return singletonDecorator(Router);
});
