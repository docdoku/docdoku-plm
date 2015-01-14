/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'views/workflows/workflow_content_list',
    'text!templates/nav/workflow_nav.html'
], function (Backbone, Mustache, singletonDecorator, WorkflowContentListView, template) {
	'use strict';
    var WorkflowNavView = Backbone.View.extend({

        el: '#workflow-nav',

        initialize: function () {
            this.render();
            this.contentView = undefined;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$changeManagementMenu) {
                App.$changeManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function () {
            this.setActive();
            this.cleanView();
            this.contentView = new WorkflowContentListView();
            this.contentView.render();
            App.appView.$content.html(this.contentView.el);
        },

        cleanView: function () {
            if (this.contentView) {
                this.contentView.remove();
            }
        }
    });
    WorkflowNavView = singletonDecorator(WorkflowNavView);
    return WorkflowNavView;
});
