/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/content.html',
    'views/workspace-creation',
    'views/workspace-home',
    'views/workspace-management-home'
], function (Backbone, Mustache, template, WorkspaceCreationView, WorkspaceHomeView, WorkspaceManagementHomeView) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        events: {
            'click .new-workspace':'navigateWorkspaceCreation',
            'click .workspace-management':'navigateWorkspaceManagement',
        },

        initialize: function () {
        },

        render: function () {

            this.$el.html(Mustache.render(template, {
                workspaces:App.config.workspaces.administratedWorkspaces,
                workspaceId:App.config.workspaceId,
                i18n: App.config.i18n,
                isCreation: window.location.hash === '#/create',
                isEdition: window.location.hash.match(/#\/workspace\/([^/]+)\/edit/) != null,
                isUsers: window.location.hash.match(/#\/workspace\/([^/]+)\/users/) != null,
                isDashboard: window.location.hash.match(/#\/workspace\/([^/]+)\/dashboard/) != null,
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

        workspaceHome : function(){
            var view = new WorkspaceHomeView();
            view.render();
            this.$('#workspace-management-content').html(view.$el);
        }

    });

    return AppView;
});
