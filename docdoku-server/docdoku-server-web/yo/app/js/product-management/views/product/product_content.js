/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'collections/configuration_items',
    'text!templates/product/product_content.html',
    'views/product/product_list',
    'views/product/product_creation_view',
    'common-objects/views/baselines/snap_baseline_view',
    'text!common-objects/templates/buttons/snap_latest_button.html',
    'text!common-objects/templates/buttons/snap_released_button.html',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/udf_button.html',
    'common-objects/views/alert',
    'common-objects/views/udf/user_defined_function'
], function (Backbone, Mustache, ConfigurationItemCollection, template, ProductListView, ProductCreationView, SnapBaselineView, snapLatestButton, snapReleasedButton, deleteButton, udfButton,  AlertView, UserDefinedFunctionView) {
    'use strict';
	var ProductContentView = Backbone.View.extend({
        partials: {
            snapLatestButton: snapLatestButton,
            snapReleasedButton: snapReleasedButton,
            deleteButton: deleteButton,
            udfButton: udfButton
        },

        events: {
            'click button.new-product': 'newProduct',
            'click button.delete': 'deleteProduct',
            'click button.udf': 'openUdfView',
            'click button.new-latest-baseline': 'createLatestBaseline',
            'click button.new-released-baseline': 'createReleasedBaseline'
        },

        initialize: function () {
            _.bindAll(this);
        },
        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();

            if(!this.configurationItemCollection){
                this.configurationItemCollection = new ConfigurationItemCollection();
            }

            if(this.productListView){
                this.productListView.remove();
            }

            this.productListView = new ProductListView({
                el: this.$('#product_table'),
                collection: this.configurationItemCollection
            }).render();
            this.bindEvent();
            return this;
        },
        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete');
            this.snapLatestBaselineButton = this.$('.new-latest-baseline');
            this.snapReleasedBaselineButton = this.$('.new-released-baseline');
        },
        bindEvent: function(){
            this.delegateEvents();
            this.productListView.on('error', this.onError);
            this.productListView.on('warning', this.onWarning);
            this.productListView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.productListView.on('snap-latest-baseline-button:display', this.changeSnapLatestBaselineButtonDisplay);
            this.productListView.on('snap-released-baseline-button:display', this.changeSnapReleasedBaselineButtonDisplay);
        },


        newProduct: function () {
            var productCreationView = new ProductCreationView();
            window.document.body.appendChild(productCreationView.render().el);
            productCreationView.on('product:created',this.configurationItemCollection.push,this.configurationItemCollection);
            productCreationView.openModal();
        },
        deleteProduct: function () {
            this.productListView.deleteSelectedProducts();
        },

        createLatestBaseline: function () {
            var snapBaselineView = new SnapBaselineView({
                model: this.productListView.getSelectedProduct(),
                type: 'LATEST'
            });
            window.document.body.appendChild(snapBaselineView.render().el);
            snapBaselineView.on('warning', this.onWarning);
            snapBaselineView.openModal();
        },
        createReleasedBaseline: function () {
            var snapBaselineView = new SnapBaselineView({
                model: this.productListView.getSelectedProduct(),
                type: 'RELEASED'
            });
            window.document.body.appendChild(snapBaselineView.render().el);
            snapBaselineView.on('warning', this.onWarning);
            snapBaselineView.openModal();
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
        },


        onError:function(model, error){
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },
        onWarning:function(model, error){
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'warning',
                message: errorMessage
            }).render().$el);
        },
        openUdfView:function(){
            var view = new UserDefinedFunctionView();
            view.render();
            document.body.appendChild(view.el);
            view.openModal();
        }

    });
    return ProductContentView;
});
