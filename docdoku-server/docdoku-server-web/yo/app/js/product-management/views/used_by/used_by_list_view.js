/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'collections/used_by_product_instance',
    //'common-objects/collections/part_collection',
    'views/used_by/used_by_list_item_view',
    'text!templates/used_by/used_by_list.html'
], function (Backbone, Mustache, UsedByProductInstanceList, /*PartList, */UsedByListItemView, template) {
    'use strict';
    var UsedByListView = Backbone.View.extend({

        className: 'used-by-items-view',

        initialize: function () {
            _.bindAll(this);

            this.linkedPartIterationId = this.options.linkedPartIterationId;
            this.linkedPart = this.options.linkedPart;

            this.productInstancesCollection = new UsedByProductInstanceList();
            this.productInstancesCollection.setLinkedPart(this.linkedPart);

            this.listenTo(this.productInstancesCollection, 'reset', this.addProductInstanceViews);

            //var that = this;
            //this.linkedDocument.getUsedByPartList(this.linkedDocumentIterationId, {
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

            //this.usedByPartViews = [];
            this.usedByProductInstanceViews = [];

            this.productInstancesCollection.fetch({reset: true});

            return this;
        },

        bindDomElements: function () {
            this.productInstancesUL = this.$('#used-by-product-instances');
            this.partsUL = this.$('#used-by-parts');
        },

        addPartViews: function () {
            this.partsCollection.each(this.addPartView.bind(this));
        },

        addProductInstanceViews: function () {
            this.productInstancesCollection.each(this.addProductInstanceView.bind(this));
        },

        addPartView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByPartViews.push(usedByView);
            this.partsUL.append(usedByView.$el);
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
