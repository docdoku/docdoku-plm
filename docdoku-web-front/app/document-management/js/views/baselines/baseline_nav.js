/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'views/baselines/baseline_content',
    'text!templates/baselines/baseline_nav.html'
], function (Backbone, Mustache, singletonDecorator, BaselineContentView, template) {

    'use strict';

    var BaselineNavView = Backbone.View.extend({

        el: '#baseline-nav',

        initialize: function () {
            this.render();
            this.contentView = undefined;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$documentManagementMenu) {
                App.$documentManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function () {
            this.setActive();
            this.cleanView();
            if(!this.contentView){
                this.contentView = new BaselineContentView();
            }
            this.contentView.render();
            App.$documentManagementContent.html(this.contentView.el);
        },

        cleanView: function () {
            if (this.contentView) {
                this.contentView.undelegateEvents();
                App.$documentManagementContent.html('');
            }
        }
    });

    BaselineNavView = singletonDecorator(BaselineNavView);
    return BaselineNavView;
});
