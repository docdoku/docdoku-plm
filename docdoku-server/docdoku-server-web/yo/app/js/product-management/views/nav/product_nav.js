/*global define,App*/
define([
    'backbone',
    "mustache",
    "common-objects/common/singleton_decorator",
    "text!templates/nav/product_nav.html",
    "views/product_content"
], function (Backbone, Mustache, singletonDecorator, template, ProductContentView) {
    var ProductNavView = Backbone.View.extend({

        el: "#product-nav",

        initialize: function () {
            this.render();
            this.productContentView = undefined;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, workspaceId: APP_CONFIG.workspaceId}));
        },

        setActive: function () {
            if (App.$productManagementMenu) {
                App.$productManagementMenu.find(".active").removeClass("active");
            }
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function () {

            this.setActive();

            if (this.productContentView) {
                this.productContentView.undelegateEvents();
            }

            this.productContentView = new ProductContentView().render();

        }

    });

    ProductNavView = singletonDecorator(ProductNavView);
    return ProductNavView;

});
