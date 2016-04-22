/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-dashboard.html'
], function (Backbone, Mustache, template ) {
    'use strict';

    var WorkspaceDashboardView = Backbone.View.extend({

        events: {

        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspace: _.findWhere(App.config.workspaces.administratedWorkspaces,{id:App.config.workspaceId})
            }));
            return this;
        }
    });

    return WorkspaceDashboardView;
});
