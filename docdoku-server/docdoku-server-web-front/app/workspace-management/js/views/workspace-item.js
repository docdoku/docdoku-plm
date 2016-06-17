/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-item.html',
    'common-objects/models/workspace',
    'common-objects/models/admin'
], function (Backbone, Mustache, template, Workspace, Admin) {
    'use strict';

    var WorkspaceItemView = Backbone.View.extend({

        className:'well-large well home-workspace',

        events: {
            'click .index-workspace':'indexWorkspace'
        },

        initialize: function () {
        },

        render: function () {

            var _this = this;

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspace:this.options.workspace,
                isAdmin:App.config.admin,
                administrated:this.options.administrated
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
        },

        indexWorkspace:function(e){
            var _this = this;
            Admin.indexWorkspace(e.target.dataset.workspaceId)
                .then(function(){
                    _this.trigger('index-workspace-success', App.config.i18n.WORKSPACE_INDEXING);
                },function(error){
                    _this.trigger('index-workspace-error', error);
                });
        }

    });

    return WorkspaceItemView;
});
