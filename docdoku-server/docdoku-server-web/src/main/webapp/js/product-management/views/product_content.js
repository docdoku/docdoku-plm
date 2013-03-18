define([
    "common-objects/collections/configuration_items",
    "text!templates/product_content.html",
    "i18n!localization/nls/product-management-strings",
    "views/product_list",
    "views/product_creation_view"
], function (
    ConfigurationItemCollection,
    template,
    i18n,
    ProductListView,
    ProductCreationView
    ) {
    var ProductContentView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#product-management-content",

        events:{
            "click button.new-product":"newProduct",
            "click button.delete-product":"deleteProduct"
        },

        initialize: function () {
            _.bindAll(this);
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));

            this.bindDomElements();

            this.productListView = new ProductListView({
                el:this.$("#product_table"),
                collection:new ConfigurationItemCollection()
            }).render();

            this.productListView.on("delete-button:display", this.changeDeleteButtonDisplay)

            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$(".delete");
        },

        newProduct:function(e){
            var productCreationView = new ProductCreationView();
            this.listenTo(productCreationView, 'product:created', this.addProductInList);
            $("body").append(productCreationView.render().el);
            productCreationView.openModal();
        },

        deleteProduct:function(){
            this.productListView.deleteSelectedProducts();
        },

        addProductInList:function(product){
            this.productListView.pushProduct(product)
        },

        changeDeleteButtonDisplay:function(state){
            if(state){
                this.deleteButton.show();
            }else{
                this.deleteButton.hide();
            }
        }

    });

    return ProductContentView;

});
