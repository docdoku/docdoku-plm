/*global _,define*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/used_by_pd_instance_list_view',
    'common-objects/models/product_instance_path_data_iteration',
    'common-objects/collections/path_data_product_instance_iterations'
], function (Backbone, Mustache, UsedByListView, ProductInstancePath, ProductInstancePathCollection) {
    'use strict';
    var UsedByGroupListView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.usedByViews = [];
            var docId = this.options.linkedDocumentId;
            var that = this;

            this.options.linkedDocument.getUsedByPathDataPdInstances(docId,{
                success: function (productInstancesArray) {
                    that.groupCollection(productInstancesArray);
                },
                error: function () {
                    //console.log('error getting used by list');
                }
            });

            return this;
        },

        groupCollection: function (productInstancesArray) {
            this.groupedMap = _.groupBy(
                productInstancesArray,
                function (productInstance) {
                    return productInstance.serialNumber;
                },
                this
            );

            _.each(_.keys(this.groupedMap), this.addListView, this);
        },

        addListView: function (key) {
            var usedByView = new UsedByListView({
                collection: new ProductInstancePathCollection(this.groupedMap[key])
            }).render();

            this.usedByViews.push(usedByView);
            this.$el.append(usedByView.$el);
        }

    });

    return UsedByGroupListView;
});
