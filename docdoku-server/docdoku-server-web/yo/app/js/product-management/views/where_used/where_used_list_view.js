/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'collections/used_by_product_instance',
    //'common-objects/collections/part_collection',
    'views/where_used/where_used_list_item_view',
    'text!templates/where_used/where_used_list.html'
], function (Backbone, Mustache, UsedByProductInstanceList, /*PartList, */WhereUsedListItemView, template) {
    'use strict';
    var WhereUsedListView = Backbone.View.extend({

        className: 'where-used-items-view',

        initialize: function () {
            _.bindAll(this);

            this.linkedPartIterationId = this.options.linkedPartIterationId;
            this.linkedPart = this.options.linkedPart;

            this.productInstancesCollection = new UsedByProductInstanceList();
            this.productInstancesCollection.setLinkedPart(this.linkedPart);

            this.listenTo(this.productInstancesCollection, 'reset', this.addProductInstanceViews);

            //var that = this;
            //this.linkedDocument.getWhereUsedPartList(this.linkedDocumentIterationId, {
            //    success: function (parts) {
            //        that.partsCollection = new PartList(parts);
            //        that.addPartViews();
            //    },
            //    error: function () {
            //        console.log('error getting where used parts list');
            //    }
            //});
        },

        render: function () {
            var data = {
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();

            //this.whereUsedPartViews = [];
            this.whereUsedProductInstanceViews = [];

            this.productInstancesCollection.fetch({reset: true});

            return this;
        },

        bindDomElements: function () {
            this.productInstancesUL = this.$('#where-used-product-instances');
            this.partsUL = this.$('#where-used-parts');
        },

        addPartViews: function () {
            this.partsCollection.each(this.addPartView.bind(this));
        },

        addProductInstanceViews: function () {
            this.productInstancesCollection.each(this.addProductInstanceView.bind(this));
        },

        addPartView: function (model) {
            var whereUsedView = new WhereUsedListItemView({
                model: model
            }).render();

            this.whereUsedPartViews.push(whereUsedView);
            this.partsUL.append(whereUsedView.$el);
        },

        addProductInstanceView: function (model) {
            var whereUsedView = new WhereUsedListItemView({
                model: model
            }).render();

            this.whereUsedProductInstanceViews.push(whereUsedView);
            this.productInstancesUL.append(whereUsedView.$el);
        }

    });

    return WhereUsedListView;
});
