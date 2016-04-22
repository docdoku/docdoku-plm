/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-home.html',
    'common-objects/models/workspace'
], function (Backbone, Mustache, template, Workspace) {
    'use strict';

    var WorkspaceHomeView = Backbone.View.extend({

        events: {
            'click .delete-workspace':'delete'
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspaceId:App.config.workspaceId
            }));
            return this;
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

    return WorkspaceHomeView;
});
