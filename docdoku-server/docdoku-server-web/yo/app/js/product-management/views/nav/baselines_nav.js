/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/baselines_nav.html',
    'views/baselines/baselines_content'
], function (Backbone, Mustache, singletonDecorator, template, BaselinesContentView) {
    'use strict';
    var BaselinesNavView = Backbone.View.extend({
        el: '#baselines-nav',

        initialize: function () {
            this.render();
            this.contentView = undefined;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$productManagementMenu) {
                App.$productManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function () {
            this.setActive();
            this.cleanView();
            if(!this.contentView){
                this.contentView = new BaselinesContentView();
            }
            this.contentView.render();
            App.$productManagementContent.html(this.contentView.el);
        },

        cleanView: function () {
            if (this.contentView) {
                this.contentView.undelegateEvents();
                App.$productManagementContent.html('');
            }
        }

    });

    BaselinesNavView = singletonDecorator(BaselinesNavView);
    return BaselinesNavView;
});
