/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({

        routes: {
            '':   'workspaceManagementHome',
            'create':   'workspaceCreation',
            'admin/dashboard':'adminDashboard',
            'workspace/:workspaceId/users':   'workspaceUsers',
            'workspace/:workspaceId/edit':   'workspaceEdit',
            'workspace/:workspaceId/dashboard':   'workspaceDashboard',
        },

        refresh:function(){
            App.appView.render();
            App.headerView.render();
	    },

        workspaceManagementHome:function(){
            App.config.workspaceId = null;
            this.refresh();
            App.appView.workspaceManagementHome();
	    },

        workspaceCreation:function(){
            App.config.workspaceId = null;
            this.refresh();
            App.appView.workspaceCreation();
        },

        workspaceUsers:function(workspaceId){
            App.config.workspaceId = workspaceId;
            this.refresh();
            App.appView.workspaceUsers();
        },

        workspaceEdit:function(workspaceId){
            App.config.workspaceId = workspaceId;
            this.refresh();
            App.appView.workspaceEdit();
        },

        workspaceDashboard:function(workspaceId){
            App.config.workspaceId = workspaceId;
            this.refresh();
            App.appView.workspaceDashboard();
        },

        adminDashboard:function(){
            this.refresh();
            App.appView.adminDashboard();
        }

    });

    return singletonDecorator(Router);
});
