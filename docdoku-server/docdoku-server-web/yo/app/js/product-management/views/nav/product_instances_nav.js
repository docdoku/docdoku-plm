/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/product_instances_nav.html',
    'views/product-instances/product_instances_content'
], function (Backbone, Mustache, singletonDecorator, template, ProductInstancesContentView) {
    'use strict';
    var ProductInstancesNavView = Backbone.View.extend({
        el: '#product-instances-nav',

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
                this.contentView = new ProductInstancesContentView();
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

    ProductInstancesNavView = singletonDecorator(ProductInstancesNavView);
    return ProductInstancesNavView;
});
