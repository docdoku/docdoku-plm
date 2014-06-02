define([
    "common-objects/collections/configuration_items",
    "text!templates/product_content.html",
    "i18n!localization/nls/product-management-strings",
    "views/product_list",
    "views/product_creation_view",
    "views/baseline/baseline_creation_view",
    "text!common-objects/templates/buttons/delete_button.html"
], function (
    ConfigurationItemCollection,
    template,
    i18n,
    ProductListView,
    ProductCreationView,
    BaselineCreationView,
    delete_button
    ) {
    var ProductContentView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#product-management-content",

        partials:{
            delete_button: delete_button
        },

        events:{
            "click button.new-product":"newProduct",
            "click button.delete":"deleteProduct",
            "click button.create-baseline":"createBaseline"
        },

        initialize: function () {
            _.bindAll(this);
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n},this.partials));

            this.bindDomElements();

            this.productListView = new ProductListView({
                el:this.$("#product_table"),
                collection:new ConfigurationItemCollection()
            }).render();

            this.productListView.on("delete-button:display", this.changeDeleteButtonDisplay);
            this.productListView.on("create-baseline-button:display", this.changeCreateBaselineButtonDisplay);

            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$(".delete");
            this.createBaselineButton = this.$(".create-baseline");
        },

        newProduct:function(){
            var productCreationView = new ProductCreationView();
            this.listenTo(productCreationView, 'product:created', this.addProductInList);
            $("body").append(productCreationView.render().el);
            productCreationView.openModal();
        },

        deleteProduct:function(){
            this.productListView.deleteSelectedProducts();
        },

        createBaseline:function(){
            var baselineCreationView = new BaselineCreationView({model:this.productListView.getSelectedProduct()});
            $("body").append(baselineCreationView.render().el);
            baselineCreationView.openModal();
        },

        addProductInList:function(product){
            this.productListView.pushProduct(product);
        },

        changeDeleteButtonDisplay:function(state){
            if(state){
                this.deleteButton.show();
            }else{
                this.deleteButton.hide();
            }
        },
        changeCreateBaselineButtonDisplay:function(state){
            if(state){
                this.createBaselineButton.show();
            }else{
                this.createBaselineButton.hide();
            }
        }

    });

    return ProductContentView;

});
