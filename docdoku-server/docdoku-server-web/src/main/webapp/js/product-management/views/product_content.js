define([
    "common-objects/collections/configuration_items",
    "text!templates/product_content.html",
    "i18n!localization/nls/product-management-strings",
    "views/product_list",
    "views/product_creation_view",
    "views/baseline/baseline_creation_view",
    "common-objects/views/baselines/snap_baseline_view",
    "text!common-objects/templates/buttons/snap_latest_button.html",
    "text!common-objects/templates/buttons/snap_released_button.html",
    "text!common-objects/templates/buttons/delete_button.html"
], function (
    ConfigurationItemCollection,
    template,
    i18n,
    ProductListView,
    ProductCreationView,
    BaselineCreationView,
    SnapBaselineView,
    snap_latest_button,
    snap_released_button,
    delete_button
    ) {
    var ProductContentView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#product-management-content",

        partials:{
            snap_latest_button: snap_latest_button,
            snap_released_button: snap_released_button,
            delete_button: delete_button
        },

        events:{
            "click button.new-product":"newProduct",
            "click button.delete":"deleteProduct",
            "click button.new-latest-baseline":"createLatestBaseline",
            "click button.new-released-baseline":"createReleasedBaseline"
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
            this.productListView.on("snap-latest-baseline-button:display", this.changeSnapLatestBaselineButtonDisplay);
            this.productListView.on("snap-released-baseline-button:display", this.changeSnapReleasedBaselineButtonDisplay);

            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$(".delete");
            this.snapLatestBaselineButton = this.$(".new-latest-baseline");
            this.snapReleasedBaselineButton = this.$(".new-released-baseline");
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

        createLatestBaseline:function(){
            var snapBaselineView = new SnapBaselineView(
                {
                    model:this.productListView.getSelectedProduct(),
                    type: "LATEST"
                });
            $("body").append(snapBaselineView.render().el);
            snapBaselineView.openModal();
        },

        createReleasedBaseline:function(){
            var snapBaselineView = new SnapBaselineView(
                {
                    model:this.productListView.getSelectedProduct(),
                    type: "RELEASED"
                });
            $("body").append(snapBaselineView.render().el);
            snapBaselineView.openModal();
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
        changeSnapLatestBaselineButtonDisplay:function(state){
            if(state){
                this.snapLatestBaselineButton.show();
            }else{
                this.snapLatestBaselineButton.hide();
            }
        },
        changeSnapReleasedBaselineButtonDisplay:function(state){
            if(state){
                this.snapReleasedBaselineButton.show();
            }else{
                this.snapReleasedBaselineButton.hide();
            }
        }

    });

    return ProductContentView;

});
