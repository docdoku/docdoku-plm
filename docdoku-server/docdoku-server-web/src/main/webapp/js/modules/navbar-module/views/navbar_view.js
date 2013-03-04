define(["common-objects/collections/configuration_items",
    "text!modules/navbar-module/templates/navbar_product_item.html"],
    function(ConfigurationItemCollection, productItemTemplate) {

    var NavBarView = Backbone.View.extend({

        el: $("div.navbar"),

        events: {
            "click .product-management": "onProductManagement"
        },

        initialize: function() {
            this.productsCollection = new ConfigurationItemCollection();
            this.$listProducts = this.$('li#product_container > ul');
            this.listenTo(this.productsCollection,'reset',this.onProductsCollectionReset);
            this.productsCollection.fetch();
        },

        onProductsCollectionReset:function(){
            var that = this;
            if(! _.isEmpty(this.productsCollection.models)){
                this.$listProducts.append("<li class='divider'></li>");
                this.productsCollection.each(function(model){
                   that.addProductInList(model);
                });
            }
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
            this.$listProducts.append(Mustache.render(productItemTemplate, configurationItem));
        },

        removeProductFromList:function(configurationItem){
            this.$listProducts.find("[data-id='"+configurationItem.getId()+"']").remove();
        }

    });

    return NavBarView;

});