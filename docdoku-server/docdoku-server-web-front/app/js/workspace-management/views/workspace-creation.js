/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-creation.html',
    'common-objects/models/workspace'
], function (Backbone, Mustache, template, Workspace) {
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
                window.location.hash = '#/workspace/'+workspace.id;
            },function(){
                console.log('Error')
                console.log(arguments)
            });
            e.preventDefault();
            return false;
        }
    });

    return WorkspaceCreationView;
});
