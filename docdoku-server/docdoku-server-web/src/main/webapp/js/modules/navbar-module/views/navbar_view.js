define(function() {

    var NavBarView = Backbone.View.extend({

        el: $("div.navbar"),

        events: {
            "click .product-management": "onProductManagement"
        },

        initialize: function() {
            this.$listProducts = this.$('li#product_container > ul');
        },

        onProductManagement: function() {
            var self = this;
            require(['modules/product-creation-module/views/product_management_view'], function(ProductManagementView) {
                var productManagementView = new ProductManagementView();

                self.listenTo(productManagementView, 'product:created', self.addProductInList);
                self.listenTo(productManagementView, 'product:removed', self.removeProductFromList);

                self.$el.after(productManagementView.render().el);
                productManagementView.openModal();
            });
        },

        addProductInList: function(configurationItem) {
            var self = this;
            require(["text!modules/navbar-module/templates/navbar_product_item.html"], function(productItemTemplate) {
                self.$listProducts.append(Mustache.render(productItemTemplate, configurationItem));
            });
        },

        removeProductFromList:function(configurationItem){
            this.$listProducts.find("[data-id="+configurationItem.getId()+"]").remove();
        }

    });

    return NavBarView;

});