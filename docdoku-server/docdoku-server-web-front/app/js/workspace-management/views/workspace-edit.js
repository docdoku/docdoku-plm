/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-edit.html',
    'common-objects/models/workspace'
], function (Backbone, Mustache, template, Workspace) {
    'use strict';

    var WorkspaceEditView = Backbone.View.extend({

        events: {
            'click .delete-workspace':'delete',
            'submit #workspace_update_form':'onSubmit'
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspace: _.findWhere(App.config.workspaces.administratedWorkspaces,{id:App.config.workspaceId})
            }));
            return this;
        },

        onSubmit:function(e){

            var description = this.$('#description').val();
            var folderLocked = this.$('#folderLocked').is(':checked');
            var $success = this.$('.success-update');
            $success.hide();

            Workspace.updateWorkspace({
                id:App.config.workspaceId,
                description:description,
                folderLocked:folderLocked
            }).then(function(){
                $success.show();
                var workspace = _.findWhere(App.config.workspaces.administratedWorkspaces,{id:App.config.workspaceId});
                workspace.description=description;
                workspace.folderLocked=folderLocked;
            },this.onError);
            e.preventDefault();
            return false;
        },
        onError:function(){
            console.log('Error')
            console.log(arguments)
        },

        delete:function(){
            if(confirm(App.config.i18n.DELETE)){
                Workspace.deleteWorkspace(App.config.workspaceId)
                    .then(function(){
                        console.log('Request sent, redirect')
                        window.location.hash = '#/';
                    });
            }


        }
    });

    return WorkspaceEditView;
});
