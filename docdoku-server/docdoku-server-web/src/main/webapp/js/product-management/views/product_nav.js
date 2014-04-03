define([
    "common-objects/common/singleton_decorator",
    "text!templates/product_nav.html",
    "i18n!localization/nls/product-management-strings",
    "views/product_content"
], function (
    singletonDecorator,
    template,
    i18n,
    ProductContentView
    ) {
    var ProductNavView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#product-nav",

        initialize: function () {
            this.render();
            this.productContentView = undefined;
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
        },

        setActive: function () {
            if(APP_VIEW.$productManagementMenu){
                APP_VIEW.$productManagementMenu.find(".active").removeClass("active");
            }
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function () {

            this.setActive();

            if(this.productContentView){
                this.productContentView.undelegateEvents();
            }

            this.productContentView = new ProductContentView().render();

        }

    });

    ProductNavView = singletonDecorator(ProductNavView);
    return ProductNavView;

});
