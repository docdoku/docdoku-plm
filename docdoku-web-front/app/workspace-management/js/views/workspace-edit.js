/*global define,App,confirm,bootbox*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-edit.html',
    'common-objects/models/workspace',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Workspace, AlertView) {
    'use strict';

    var WorkspaceEditView = Backbone.View.extend({

        events: {
            'click .delete-workspace':'deleteWorkspace',
            'submit #workspace_update_form':'onSubmit'
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspace: _.findWhere(App.config.workspaces.administratedWorkspaces,{id:App.config.workspaceId})
            }));
            this.$notifications = this.$('.notifications');
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
            },this.onError.bind(this));
            e.preventDefault();
            return false;
        },

        deleteWorkspace:function(){
            var _this = this;
            bootbox.confirm(
                '<h4>'+App.config.i18n.DELETE_WORKSPACE_QUESTION+'</h4>'+
                '<p><i class="fa fa-warning"></i> '+App.config.i18n.DELETE_WORKSPACE_TEXT+'</p>',
                App.config.i18n.CANCEL,
                App.config.i18n.DELETE,
                function(result){
                if(result){
                    Workspace.deleteWorkspace(App.config.workspaceId)
                        .then(_this.onDeleteWorkspaceSuccess.bind(_this),_this.onError.bind(_this));
                }
            });
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        onDeleteWorkspaceSuccess:function(){
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                deleting: true
            }));
        }
    });

    return WorkspaceEditView;
});
