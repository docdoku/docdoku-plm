/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'collections/used_by_product_instance',
    'collections/used_by_configuration_item',
    'views/used_by/used_by_list_item_view',
    'text!templates/used_by/used_by_list.html'
], function (Backbone, Mustache, UsedByProductInstanceList, UsedByConfigurationItemList, UsedByListItemView, template) {
    'use strict';
    var UsedByListView = Backbone.View.extend({

        className: 'used-by-items-view',

        initialize: function () {
            _.bindAll(this);

            this.linkedPartIterationId = this.options.linkedPartIterationId;
            this.linkedPart = this.options.linkedPart;

            this.configurationItemsCollection = new UsedByConfigurationItemList();
            this.configurationItemsCollection.setLinkedPart(this.linkedPart);
            this.listenTo(this.configurationItemsCollection, 'reset', this.addConfigurationItemViews);

            this.productInstancesCollection = new UsedByProductInstanceList();
            this.productInstancesCollection.setLinkedPart(this.linkedPart);
            this.listenTo(this.productInstancesCollection, 'reset', this.addProductInstanceViews);
        },

        render: function () {
            var data = {
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();

            this.usedByConfigurationItemViews = [];
            this.usedByProductInstanceViews = [];

            this.configurationItemsCollection.fetch({reset: true});
            this.productInstancesCollection.fetch({reset: true});

            return this;
        },

        bindDomElements: function () {
            this.configurationItemsUL = this.$('#used-by-configuration-items');
            this.productInstancesUL = this.$('#used-by-product-instances');
        },

        addConfigurationItemView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByConfigurationItemViews.push(usedByView);
            this.configurationItemsUL.append(usedByView.$el);
        },

        addConfigurationItemViews: function () {
            this.configurationItemsCollection.each(this.addConfigurationItemView.bind(this));
        },

        addProductInstanceViews: function () {
            this.productInstancesCollection.each(this.addProductInstanceView.bind(this));
        },

        addProductInstanceView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByProductInstanceViews.push(usedByView);
            this.productInstancesUL.append(usedByView.$el);
        }

    });

    return UsedByListView;
});
