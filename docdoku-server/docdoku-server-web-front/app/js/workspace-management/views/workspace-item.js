/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-administrated-item.html',
    'text!templates/workspace-non-administrated-item.html',
    'common-objects/models/workspace'
], function (Backbone, Mustache, adminTemplate, nonAdminTemplate, Workspace) {
    'use strict';

    var WorkspaceItemView = Backbone.View.extend({

        className:'well-large well home-workspace',

        events: {
        },

        initialize: function () {
        },

        render: function () {

            var _this = this;

            var template = this.options.administrated ? adminTemplate : nonAdminTemplate;

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspace:this.options.workspace
            }));

            Workspace.getStatsOverView(this.options.workspace.id).then(function(stats){
                _this.$('.documents-count').text(stats.documents);
                _this.$('.parts-count').text(stats.parts);
                _this.$('.users-count').text(stats.users);
                _this.$('.products-count').text(stats.products);
            });

            return this;
        },

        newWorkspace:function(){
            window.location.href='#/create';
        }
    });

    return WorkspaceItemView;
});
