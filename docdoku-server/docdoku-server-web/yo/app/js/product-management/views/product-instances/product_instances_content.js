/*global _.define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/product_instances',
    'collections/configuration_items',
    'text!templates/product-instances/product_instances_content.html',
    'views/product-instances/product_instances_list',
    'views/product-instances/product_instances_creation',
    'text!common-objects/templates/buttons/delete_button.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, ProductInstancesCollection, ConfigurationItemCollection, template, ProductInstancesListView, ProductInstanceCreationView, deleteButton, AlertView) {
    'use strict';
    var BaselinesContentView = Backbone.View.extend({

        partials: {
            deleteButton: deleteButton
        },

        events: {
            'click button.new-product-instance': 'newProductInstance',
            'click button.delete': 'deleteBaseline'
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
            this.configurationItemCollection.fetch({
                success: this.fillProductList,
                error: this.onError
            });

            this.bindEvent();
            this.createProductInstancesView();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete');
            this.$inputProductId = this.$('#inputProductId');
        },

        bindEvent: function () {
            var _this = this;
            this.$inputProductId.change(function () {
                _this.createProductInstancesView();
            });
            this.delegateEvents();
        },

        newProductInstance: function () {
            var self = this;
            this.lockButton(true);
            var productInstanceCreationView = new ProductInstanceCreationView({
                collection: self.collection
            });
            window.document.body.appendChild(productInstanceCreationView.render().el);
            productInstanceCreationView.openModal();
            self.lockButton(false);
        },

        fillProductList: function (list) {
            var self = this;
            if (list) {
                list.each(function (product) {
                    self.$inputProductId.append('<option value="' + product.getId() + '"' + '>' + product.getId() + '</option>');
                });
                this.$inputProductId.combobox({bsVersion: 2});
            }
        },

        createProductInstancesView: function () {
            if (this.listView) {
                this.listView.remove();
                this.changeDeleteButtonDisplay(false);
            }
            if (this.$inputProductId.val()) {
                this.collection = new ProductInstancesCollection({}, {productId: this.$inputProductId.val()});
            } else {
                this.collection = new ProductInstancesCollection({});
            }
            this.listView = new ProductInstancesListView({
                collection: this.collection
            }).render();
            this.$el.append(this.listView.el);
            this.listView.on('error', this.onError);
            this.listView.on('warning', this.onWarning);
            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
        },

        deleteBaseline: function () {
            this.listView.deleteSelectedProductInstances();
        },

        changeDeleteButtonDisplay: function (state) {
            if (state) {
                this.deleteButton.show();
            } else {
                this.deleteButton.hide();
            }
        },

        lockButton: function (state) {
            if (state) {
                $('button.new-product-instance').attr('disabled', 'disabled');
            } else {
                $('button.new-product-instance').removeAttr('disabled');
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
        }
    });

    return BaselinesContentView;
});
