/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-management-home.html'
], function (Backbone, Mustache, template) {
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
                workspaces:App.config.workspaces
            }));
            return this;
        },

        newWorkspace:function(){
            window.location.href='#/create';
        }
    });

    return WorkspaceManagementHomeView;
});
