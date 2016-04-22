/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator',
    'common-objects/contextResolver'
],
function (Backbone, singletonDecorator, ContextResolver) {
    'use strict';
    var Router = Backbone.Router.extend({

        routes: {
            '':   'workspaceManagementHome',
            'create':   'workspaceCreation',
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
        }

    });

    return singletonDecorator(Router);
});
