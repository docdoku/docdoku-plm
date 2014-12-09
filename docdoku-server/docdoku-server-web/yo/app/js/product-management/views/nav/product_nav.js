/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/product_nav.html',
    'views/product/product_content'
], function (Backbone, Mustache, singletonDecorator, template, ProductContentView) {
    'use strict';
    var ProductNavView = Backbone.View.extend({
        el: '#product-nav',

        initialize: function () {
            this.render();
            this.productContentView = undefined;
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
            if(!this.productContentView){
                this.productContentView = new ProductContentView();
            }
            this.productContentView.render();
            App.$productManagementContent.html(this.productContentView.el);
        },

        cleanView: function () {
            if (this.productContentView) {
                this.productContentView.undelegateEvents();
                App.$productManagementContent.html('');
            }
        }

    });

    ProductNavView = singletonDecorator(ProductNavView);
    return ProductNavView;

});
