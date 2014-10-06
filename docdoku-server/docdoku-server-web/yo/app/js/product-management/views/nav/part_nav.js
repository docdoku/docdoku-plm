/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/part_nav.html',
    'views/part_content'
], function (Backbone, Mustache, singletonDecorator, template, PartContentView) {
	'use strict';
    var PartNavView = Backbone.View.extend({

        el: '#part-nav',

        initialize: function () {
            this.render();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            $('#product-management-menu').find('.active').removeClass('active');
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function (query) {
            this.setActive();

            if (this.partContentView) {
                this.partContentView.undelegateEvents();
            }

            this.partContentView = new PartContentView().setQuery(query).render();
        }
    });

    PartNavView = singletonDecorator(PartNavView);
    return PartNavView;
});
