/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/milestone_nav.html',
    'views/milestones/milestone_content'
], function (Backbone, Mustache, singletonDecorator, template, MilestoneContentView) {
	'use strict';
    var MilestoneNavView = Backbone.View.extend({
        el: '#milestone-nav',

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
            this.contentView = new MilestoneContentView().render();
	        App.appView.$content.html(this.contentView.el);
        },

        cleanView: function () {
            if (this.contentView) {
                this.contentView.remove();
            }
        }
    });

    MilestoneNavView = singletonDecorator(MilestoneNavView);
    return MilestoneNavView;
});
