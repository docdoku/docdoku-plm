/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-management-home.html',
    'common-objects/models/workspace',
    'views/workspace-item',
], function (Backbone, Mustache, template, Workspace, WorkspaceItemView) {
    'use strict';

    var WorkspaceManagementHomeView = Backbone.View.extend({

        events: {
            'click .new-workspace':'newWorkspace'
        },

        initialize: function () {
        },

        render: function () {

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspaceId:App.config.workspaceId,
                isAdmin:App.config.admin
            }));

            var $administratedWorkspaces = this.$('.administrated-workspaces');
            var $nonAdministratedWorkspaces  = this.$('.non-administrated-workspaces');
            $administratedWorkspaces.empty();
            $nonAdministratedWorkspaces.empty();
            _.each(App.config.workspaces.administratedWorkspaces,function(workspace){
                var view = new WorkspaceItemView({administrated:true,workspace:workspace});
                $administratedWorkspaces.append(view.render().$el);
            });

            _.each(App.config.workspaces.nonAdministratedWorkspaces,function(workspace){
                var view = new WorkspaceItemView({administrated:false,workspace:workspace});
                $nonAdministratedWorkspaces.append(view.render().$el);
            });

            return this;
        },

        newWorkspace:function(){
            window.location.href='#/create';
        }
    });

    return WorkspaceManagementHomeView;
});
