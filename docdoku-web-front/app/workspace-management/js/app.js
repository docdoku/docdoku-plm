/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/content.html',
    'views/workspace-edit',
    'views/workspace-creation',
    'views/workspace-users',
    'views/workspace-notifications',
    'views/workspace-dashboard',
    'views/workspace-management-home',
    'views/admin-dashboard'
], function (Backbone, Mustache, template, WorkspaceEditView, WorkspaceCreationView, WorkspaceUsersView, WorkspaceNotificationsView, WorkspaceDashboardView, WorkspaceManagementHomeView, AdminDashboardView) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        events: {
            'click .new-workspace':'navigateWorkspaceCreation',
            'click .workspace-management':'navigateWorkspaceManagement'
        },

        initialize: function () {
        },

        render: function () {
            var isEditionRegex = new RegExp('#/workspace/'+App.config.workspaceId+'/edit','g');
            var isUsersRegex = new RegExp('#/workspace/'+App.config.workspaceId+'/users','g');
            var isNotificationsRegex = new RegExp('#/workspace/'+App.config.workspaceId+'/notifications','g');
            var isDashboardRegex = new RegExp('#/workspace/'+App.config.workspaceId+'/dashboard','g');
            this.$el.html(Mustache.render(template, {
                isAdmin:App.config.admin,
                administratedWorkspaces:App.config.workspaces.administratedWorkspaces,
                nonAdministratedWorkspaces:App.config.workspaces.nonAdministratedWorkspaces,
                workspaceId:App.config.workspaceId,
                i18n: App.config.i18n,
                isCreation: window.location.hash === '#/create',
                isEdition: window.location.hash.match(isEditionRegex) !== null,
                isUsers: window.location.hash.match(isUsersRegex) !== null,
                isNotifications: window.location.hash.match(isNotificationsRegex) !== null,
                isDashboard: window.location.hash.match(isDashboardRegex) !== null
            })).show();
            return this;
        },

        navigateWorkspaceCreation:function(){
            window.location.hash = '#/create';
        },

        navigateWorkspaceManagement:function(){
            window.location.hash = '#/';
        },

        workspaceManagementHome : function(){
            var view = new WorkspaceManagementHomeView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);
        },

        workspaceCreation : function(){
            var view = new WorkspaceCreationView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);
        },
        workspaceUsers : function(){
            var view = new WorkspaceUsersView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);
        },
        workspaceNotifications : function(){
            var view = new WorkspaceNotificationsView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);
        },
        workspaceEdit : function(){
            var view = new WorkspaceEditView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);
        },
        workspaceDashboard : function(){
            var view = new WorkspaceDashboardView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);
        },
        adminDashboard:function(){
            var view = new AdminDashboardView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);

        }

    });

    return AppView;
});
