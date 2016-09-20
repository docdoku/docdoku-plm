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
            'workspace/:workspaceId/notifications':   'workspaceNotifications',
            'workspace/:workspaceId/edit':   'workspaceEdit',
            'workspace/:workspaceId/dashboard':   'workspaceDashboard',
            'workspace/:workspaceId/*path':   'workspaceUsers'
        },

        checkWorkspaceAdmin:function(workspaceId){

            var isAdmin = App.config.workspaces.administratedWorkspaces.filter(function(workspace){
                return workspace.id === workspaceId;
            }).length > 0;

            if(!isAdmin) {
                window.location.hash = '#';
            }
            return isAdmin;
        },

        checkRootAdmin:function(){
            if(!App.config.admin){
                window.location.hash = '#';
            }
            return App.config.admin;
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
            if(this.checkWorkspaceAdmin(workspaceId)){
                App.config.workspaceId = workspaceId;
                this.refresh();
                App.appView.workspaceUsers();
            }
        },

        workspaceNotifications:function(workspaceId){
            if(this.checkWorkspaceAdmin(workspaceId)){
                App.config.workspaceId = workspaceId;
                this.refresh();
                App.appView.workspaceNotifications();
            }
        },

        workspaceEdit:function(workspaceId){
            if(this.checkWorkspaceAdmin(workspaceId)) {
                App.config.workspaceId = workspaceId;
                this.refresh();
                App.appView.workspaceEdit();
            }
        },

        workspaceDashboard:function(workspaceId){
            if(this.checkWorkspaceAdmin(workspaceId)){
                App.config.workspaceId = workspaceId;
                this.refresh();
                App.appView.workspaceDashboard();
            }
        },

        adminDashboard:function(){
            if(this.checkRootAdmin()){
                this.refresh();
                App.appView.adminDashboard();
            }
        }

    });

    return singletonDecorator(Router);
});
