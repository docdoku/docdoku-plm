/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'collections/configuration_items',
    'common-objects/collections/part_collection',
    'models/configuration_item',
    'text!templates/product/product_content.html',
    'views/product/product_list',
    'views/product/product_creation_view',
    'views/baselines/baseline_creation_view',
    'text!common-objects/templates/buttons/snap_button.html',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/udf_button.html',
    'common-objects/views/alert',
    'common-objects/views/udf/user_defined_function'
], function (Backbone, Mustache, ConfigurationItemCollection, PartCollection, ConfigurationItem, template, ProductListView, ProductCreationView, BaselineCreationView, snapButton, deleteButton, udfButton,  AlertView, UserDefinedFunctionView) {
    'use strict';
	var ProductContentView = Backbone.View.extend({
        partials: {
            snapButton: snapButton,
            deleteButton: deleteButton,
            udfButton: udfButton
        },

        events: {
            'click button.new-product': 'newProduct',
            'click button.delete': 'deleteProduct',
            'click button.udf': 'openUdfView',
            'click button.new-baseline': 'createBaseline'
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

            this.partsCollection = new PartCollection();
            this.partsCollection.on('page-count:fetch',this.checkForPartCount);
            this.partsCollection.fetchPageCount();

            return this;
        },
        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.snapBaselineButton = this.$('.new-baseline');
            this.deleteButton = this.$('.delete');
        },
        bindEvent: function(){
            this.delegateEvents();
            this.productListView.on('error', this.onError);
            this.productListView.on('warning', this.onWarning);
            this.productListView.on('info', this.onInfo);
            this.productListView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.productListView.on('create-baseline-button:display', this.changeSnapBaselineButtonDisplay);
        },

        newProduct: function () {
            var productCreationView = new ProductCreationView();
            window.document.body.appendChild(productCreationView.render().el);
            productCreationView.on('product:created',this.configurationItemCollection.push,this.configurationItemCollection);
            productCreationView.openModal();
        },

        createBaseline: function () {
            var baselineCreationView = new BaselineCreationView({
                model: this.productListView.getSelectedProduct()
            });
            window.document.body.appendChild(baselineCreationView.render().el);
            baselineCreationView.on('warning', this.onWarning);
            baselineCreationView.on('info', this.onInfo);
            baselineCreationView.openModal();
        },

        deleteProduct: function () {
            this.productListView.deleteSelectedProducts();
        },

        onInfo:function(message){
            this.$notifications.append(new AlertView({
                type: 'info',
                message: message
            }).render().$el);
        },

        changeSnapBaselineButtonDisplay: function (state) {
            this.snapBaselineButton.toggle(state);
        },

        changeDeleteButtonDisplay: function (state) {
            this.deleteButton.toggle(state);
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
        },

        checkForPartCount:function(){
            if(!this.partsCollection.pageCount){
                this.$notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.CREATE_PART_BEFORE_PRODUCT
                }).render().$el);
            }
        }

    });
    return ProductContentView;
});
