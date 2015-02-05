/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/product_instances',
    'collections/configuration_items',
    'common-objects/collections/baselines',
    'text!templates/product-instances/product_instances_content.html',
    'views/product-instances/product_instances_list',
    'views/product-instances/product_instances_creation',
    'text!common-objects/templates/buttons/delete_button.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, ProductInstancesCollection, ConfigurationItemCollection, BaselinesCollection, template, ProductInstancesListView, ProductInstanceCreationView, deleteButton, AlertView) {
    'use strict';
    var ProductInstancesContentView = Backbone.View.extend({

        partials: {
            deleteButton: deleteButton
        },

        events: {
            'click button.new-product-instance': 'newProductInstance',
            'click button.delete': 'deleteProductInstances'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();

            this.$inputProductId.typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products', function (data) {
                        var ids = [];
                        _(data).each(function (d) {
                            ids.push(d.id);
                        });
                        process(ids);
                    });
                }
            });

            var self = this ;
            new BaselinesCollection({}, {productId: ""}).fetch({
                success: function (list) {
                   if(!list.length){
                       self.$notifications.append(new AlertView({
                           type: 'info',
                           message: App.config.i18n.CREATE_BASELINE_BEFORE_PRODUCT_INSTANCE
                       }).render().$el);

                   }
                }
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
            var productInstanceCreationView = new ProductInstanceCreationView({
                collection: this.collection
            });
            window.document.body.appendChild(productInstanceCreationView.render().el);
            productInstanceCreationView.openModal();
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

        deleteProductInstances: function () {
            this.listView.deleteSelectedProductInstances();
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
        }
    });

    return ProductInstancesContentView;
});
