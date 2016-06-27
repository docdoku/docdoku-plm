/*global _,define*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/used_by_list_view',
    'common-objects/models/product_instance',
    'common-objects/collections/product_instances'
], function (Backbone, Mustache, UsedByListView, ProductInstance, ProductInstancesCollection) {
    'use strict';
    var UsedByGroupListView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.usedByViews = [];

            var that = this;

            this.options.linkedPart.getUsedByProductInstances({
                success: function (productInstancesArray) {
                    that.groupCollection(productInstancesArray);
                }
            });

            return this;
        },

        groupCollection: function (productInstancesArray) {
            this.groupedMap = _.groupBy(
                productInstancesArray,
                function (productInstance) {
                    return productInstance.configurationItemId;
                },
                this
            );

            _.each(_.keys(this.groupedMap), this.addListView, this);
        },

        addListView: function (key) {
            var usedByView = new UsedByListView({
                collection: new ProductInstancesCollection(this.groupedMap[key])
            }).render();

            this.usedByViews.push(usedByView);
            this.$el.append(usedByView.$el);
        }

    });

    return UsedByGroupListView;
});
