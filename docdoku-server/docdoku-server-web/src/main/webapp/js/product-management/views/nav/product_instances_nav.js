/*global APP_VIEW*/
define([
    "common-objects/common/singleton_decorator",
    "text!templates/nav/product_instances_nav.html",
    "i18n!localization/nls/product-instances-strings",
    "views/product-instances/product_instances_content"
], function (
    singletonDecorator,
    template,
    i18n,
    ProductInstancesContentView
    ) {
    var ProductInstancesNavView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#product-instances-nav",

        initialize: function () {
            this.render();
            this.contentView = undefined;
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

        showContent: function (elementId) {
            this.setActive();
            this.clearView();
            this.contentView = new ProductInstancesContentView().render();
            $(elementId).html(this.contentView.el);
        },

        clearView: function(){
            if(this.contentView){
                this.contentView.undelegateEvents();
                this.contentView.remove();
            }
        }

    });

    ProductInstancesNavView = singletonDecorator(ProductInstancesNavView);
    return ProductInstancesNavView;
});
