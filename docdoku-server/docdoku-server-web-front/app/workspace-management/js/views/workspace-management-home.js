/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-management-home.html',
    'common-objects/models/workspace',
    'common-objects/views/alert',
    'views/workspace-item'
], function (Backbone, Mustache, template, Workspace, AlertView, WorkspaceItemView) {
    'use strict';

    var WorkspaceManagementHomeView = Backbone.View.extend({

        events: {
            'click .new-workspace':'newWorkspace'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspaceId:App.config.workspaceId,
                isAdmin:App.config.admin
            }));

            this.$notifications = this.$('.notifications');
            var $administratedWorkspaces = this.$('.administrated-workspaces');
            var $nonAdministratedWorkspaces  = this.$('.non-administrated-workspaces');
            $administratedWorkspaces.empty();
            $nonAdministratedWorkspaces.empty();

            _.each(App.config.workspaces.administratedWorkspaces,function(workspace){
                var view = new WorkspaceItemView({administrated:true,workspace:workspace});
                $administratedWorkspaces.append(view.render().$el);
                _this.listenTo(view,'index-workspace-success',_this.onInfo.bind(_this));
                _this.listenTo(view,'index-workspace-error',_this.onError.bind(_this));
            });

            _.each(App.config.workspaces.nonAdministratedWorkspaces,function(workspace){
                var view = new WorkspaceItemView({administrated:false,workspace:workspace});
                $nonAdministratedWorkspaces.append(view.render().$el);
            });

            return this;
        },

        newWorkspace:function(){
            window.location.href='#/create';
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        onInfo:function(message){
            this.$notifications.append(new AlertView({
                type: 'info',
                message: message
            }).render().$el);
        }

    });

    return WorkspaceManagementHomeView;
});
