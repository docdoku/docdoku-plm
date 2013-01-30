define(["modules/product-creation-module/views/product_creation_view"], function(ProductCreationView) {

    var NavBarView = Backbone.View.extend({

        el: $("div.navbar"),

        events: {
            "click #product-creation": "onProductCreation"
        },

        onProductCreation: function() {
            var productCreationView = new ProductCreationView();
            this.$el.after(productCreationView.render().el);
            productCreationView.openModal();
            this.listenTo(productCreationView, 'product:created', this.refreshProductsList);
        },

        refreshProductsList: function() {

        }

    });

    return NavBarView;

});