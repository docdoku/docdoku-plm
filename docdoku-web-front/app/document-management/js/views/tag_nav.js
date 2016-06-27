/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator',
    'common-objects/views/base',
    'views/tag_list',
    'text!templates/tag_nav.html'
], function (Backbone,singletonDecorator, BaseView, TagListView, template) {

    'use strict';

    var TagNavView = BaseView.extend({
        template: template,
        el: '#tag-nav',
        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            var toggleTarget = '[data-target="#items-' + this.cid + '"]';
            this.events['click ' + toggleTarget] = 'toggle';

            var that = this;

            Backbone.Events.on('refreshTagNavViewCollection', function () {
                that.tagsView.collection.fetch({reset: true});
            });

            this.render();
        },
        rendered: function () {
            this.tagsView = this.addSubView(
                new TagListView({
                    el: '#items-' + this.cid
                })
            );
            this.tagsView.bind('shown', this.onTagsViewShown);
            this.bind('shown', this.shown);
            this.bind('hidden', this.hidden);
        },
        show: function (tag) {
            this.tag = tag;
            this.isOpen = true;
            this.tagsView.showTag(this.tag);
            if (!this.tag) {
                App.router.navigate(App.config.workspaceId + '/tags', {trigger: false});
            }
            this.trigger('shown');
        },
        hide: function () {
            this.isOpen = false;
            this.tagsView.hide();
            App.router.navigate(App.config.workspaceId + '/tags', {trigger: false});
            this.trigger('hidden');
        },
        shown: function () {
            this.$el.addClass('open');
        },
        hidden: function () {
            this.$el.removeClass('open');
        },
        toggle: function () {
            if (this.isOpen) {
                this.hide();
            } else {
                this.show();
            }
            return false;
        }
    });
    TagNavView = singletonDecorator(TagNavView);
    return TagNavView;
});
