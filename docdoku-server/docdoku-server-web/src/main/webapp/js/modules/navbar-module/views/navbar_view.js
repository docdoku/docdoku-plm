define(["modules/product-creation-module/views/product_creation_view"], function(ProductCreationView) {

    var NavBarView = Backbone.View.extend({

        el: $("div.navbar"),

        events: {
            "click #product-creation": "onProductCreation"
        },

        initialize: function() {
            this.$listProducts = this.$('li#product_container > ul');
        },

        onProductCreation: function() {
            var productCreationView = new ProductCreationView();
            this.listenTo(productCreationView, 'product:created', this.addProductInList);
            this.$el.after(productCreationView.render().el);
            productCreationView.openModal();
        },

        addProductInList: function(configurationItem) {
            var self = this;
            require(["text!modules/navbar-module/templates/navbar_product_item.html"], function(productItemTemplate) {
                self.$listProducts.append(Mustache.render(productItemTemplate, configurationItem));
            });
        }

    });

    return NavBarView;

});