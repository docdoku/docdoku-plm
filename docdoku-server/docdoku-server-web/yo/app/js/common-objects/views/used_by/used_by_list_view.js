/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/part_collection',
    'common-objects/collections/product_instances',
    'common-objects/collections/used_by/used_by_document',
    'common-objects/views/used_by/used_by_list_item_view',
    'common-objects/views/part/used_by_list_item_view',
    'common-objects/views/part/used_by_pd_instance_group_list_view',
    'text!common-objects/templates/document/used_by_list.html'
], function (Backbone, Mustache, PartList, ProductInstanceList, UsedByDocumentList, UsedByListItemView,UsedByProductInstanceListItemView, UsedByGroupListView,template) {
    'use strict';
    var UsedByListView = Backbone.View.extend({

        className: 'used-by-items-view',

        initialize: function () {
            _.bindAll(this);

            this.linkedDocumentIterationId = this.options.linkedDocumentIterationId;
            this.linkedDocument = this.options.linkedDocument;

            this.documentsCollection = new UsedByDocumentList();
            this.documentsCollection.setLinkedDocumentIterationId(this.linkedDocumentIterationId);
            this.documentsCollection.setLinkedDocument(this.linkedDocument);

            this.listenTo(this.documentsCollection, 'reset', this.addDocumentViews);

            var that = this;
            this.linkedDocument.getUsedByPartList(this.linkedDocumentIterationId, {
                success: function (parts) {
                    that.partsCollection = new PartList(parts);
                    that.addPartViews();
                }
            });
            this.linkedDocument.getUsedByProductInstances(this.linkedDocumentIterationId, {
                success: function (productInstanceIterations) {
                    that.productInstancesCollection = new ProductInstanceList(productInstanceIterations);
                    that.addProductInstanceViews();
                }
            });

        },

        render: function () {
            var data = {
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();

            this.usedByPartViews = [];
            this.usedByDocumentViews = [];
            this.usedByProductInstanceViews = [];
            this.documentsCollection.fetch({reset: true});

            this.initUsedByGroup();
            return this;
        },

        bindDomElements: function () {
            this.documentsUL = this.$('#used-by-documents');
            this.partsUL = this.$('#used-by-parts');
            this.productInstancesUL = this.$('#used-by-product-instances');
        },

        addPartViews: function () {
            this.partsCollection.each(this.addPartView.bind(this));
        },

        addProductInstanceViews: function () {
            this.productInstancesCollection.each(this.addProductInstanceView.bind(this));
        },

        addDocumentViews: function () {
            this.documentsCollection.each(this.addDocumentView.bind(this));
        },

        addPartView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByPartViews.push(usedByView);
            this.partsUL.append(usedByView.$el);
        },
        addProductInstanceView: function (model) {
            var usedByView = new UsedByProductInstanceListItemView({
                model: model
            }).render();

            this.usedByProductInstanceViews.push(usedByView);
            this.productInstancesUL.append(usedByView.$el);
        },


        addDocumentView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByDocumentViews.push(usedByView);
            this.documentsUL.append(usedByView.$el);
        },
        initUsedByGroup:function(){
            this.usedByGroupListView = new UsedByGroupListView({
                linkedDocument: this.linkedDocument,
                linkedDocumentId: this.linkedDocumentIterationId
            }).render();

            /* Add the usedByGroupListView to the tab */
            this.$('#used-by-group-list-view').html(this.usedByGroupListView.el);
        }

    });

    return UsedByListView;
});
