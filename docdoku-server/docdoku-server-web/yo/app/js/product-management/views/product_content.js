/*global define*/
define([
    'backbone',
    "mustache",
    "common-objects/collections/configuration_items",
    "text!templates/product_content.html",
    "views/product_list",
    "views/product_creation_view",
    "views/baseline/baseline_creation_view",
    "common-objects/views/baselines/snap_baseline_view",
    "text!common-objects/templates/buttons/snap_latest_button.html",
    "text!common-objects/templates/buttons/snap_released_button.html",
    "text!common-objects/templates/buttons/delete_button.html"
], function (Backbone, Mustache, ConfigurationItemCollection, template, ProductListView, ProductCreationView, BaselineCreationView, SnapBaselineView, snapLatestButton, snapReleasedButton, deleteButton) {
    var ProductContentView = Backbone.View.extend({

        el: "#product-management-content",

        partials: {
            snapLatestButton: snapLatestButton,
            snapReleasedButton: snapReleasedButton,
            deleteButton: deleteButton
        },

        events: {
            "click button.new-product": "newProduct",
            "click button.delete": "deleteProduct",
            "click button.new-latest-baseline": "createLatestBaseline",
            "click button.new-released-baseline": "createReleasedBaseline"
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}, this.partials));

            this.bindDomElements();

            this.productListView = new ProductListView({
                el: this.$("#product_table"),
                collection: new ConfigurationItemCollection()
            }).render();

            this.productListView.on("delete-button:display", this.changeDeleteButtonDisplay);
            this.productListView.on("snap-latest-baseline-button:display", this.changeSnapLatestBaselineButtonDisplay);
            this.productListView.on("snap-released-baseline-button:display", this.changeSnapReleasedBaselineButtonDisplay);

            return this;
        },

        bindDomElements: function () {
            this.deleteButton = this.$(".delete");
            this.snapLatestBaselineButton = this.$(".new-latest-baseline");
            this.snapReleasedBaselineButton = this.$(".new-released-baseline");
        },

        newProduct: function () {
            var productCreationView = new ProductCreationView();
            this.listenTo(productCreationView, 'product:created', this.addProductInList);
            window.document.body.appendChild(productCreationView.render().el);
            productCreationView.openModal();
        },

        deleteProduct: function () {
            this.productListView.deleteSelectedProducts();
        },

        createLatestBaseline: function () {
            var snapBaselineView = new SnapBaselineView(
                {
                    model: this.productListView.getSelectedProduct(),
                    type: "LATEST"
                });
            window.document.body.appendChild(snapBaselineView.render().el);
            snapBaselineView.openModal();
        },

        createReleasedBaseline: function () {
            var snapBaselineView = new SnapBaselineView(
                {
                    model: this.productListView.getSelectedProduct(),
                    type: "RELEASED"
                });
            window.document.body.appendChild(snapBaselineView.render().el);
            snapBaselineView.openModal();
        },

        addProductInList: function (product) {
            this.productListView.pushProduct(product);
        },

        changeDeleteButtonDisplay: function (state) {
            if (state) {
                this.deleteButton.show();
            } else {
                this.deleteButton.hide();
            }
        },
        changeSnapLatestBaselineButtonDisplay: function (state) {
            if (state) {
                this.snapLatestBaselineButton.show();
            } else {
                this.snapLatestBaselineButton.hide();
            }
        },
        changeSnapReleasedBaselineButtonDisplay: function (state) {
            if (state) {
                this.snapReleasedBaselineButton.show();
            } else {
                this.snapReleasedBaselineButton.hide();
            }
        }

    });

    return ProductContentView;

});
