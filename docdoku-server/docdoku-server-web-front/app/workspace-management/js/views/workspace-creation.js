/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-creation.html',
    'common-objects/models/workspace',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Workspace, AlertView) {
    'use strict';

    var WorkspaceCreationView = Backbone.View.extend({

        events: {
            'submit #workspace_creation_form':'onSubmit'
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));
            this.$notifications = this.$('.notifications');
            return this;
        },

        onSubmit:function(e){
            var workspaceId = this.$('#workspace-id').val();
            var description = this.$('#description').val();
            var folderLocked = this.$('#folderLocked').is(':checked');

            Workspace.createWorkspace({
                id:workspaceId,
                description:description,
                folderLocked:folderLocked
            }).then(function(workspace){
                App.config.workspaces.administratedWorkspaces.push(workspace);
                App.config.workspaces.allWorkspaces.push(workspace);
                window.location.hash = '#/';
            },this.onError.bind(this));

            e.preventDefault();
            return false;
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        }

    });

    return WorkspaceCreationView;
});
