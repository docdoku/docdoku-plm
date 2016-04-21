/*global _,define*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/used_by_pd_instance_list_view',
    'common-objects/collections/path_data_iterations'
], function (Backbone, Mustache, UsedByListView, PathDataIterations) {
    'use strict';
    var UsedByGroupListView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.usedByViews = [];
            var docId = this.options.linkedDocumentId;
            var that = this;

            this.options.linkedDocument.getInversePathDataLinks(docId,{
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
                    return productInstance.serialNumber;
                },
                this
            );

            _.each(_.keys(this.groupedMap), this.addListView, this);
        },

        addListView: function (key) {
            var usedByView = new UsedByListView({
                collection: new PathDataIterations(this.groupedMap[key])
            }).render();

            this.usedByViews.push(usedByView);
            this.$el.append(usedByView.$el);
        }

    });

    return UsedByGroupListView;
});
