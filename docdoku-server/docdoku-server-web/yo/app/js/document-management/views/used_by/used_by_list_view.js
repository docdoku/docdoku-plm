/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/part_collection',
    'collections/used_by_document',
    'views/used_by/used_by_list_item_view',
    'text!templates/used_by/used_by_list.html'
], function (Backbone, Mustache, PartList, UsedByDocumentList, UsedByListItemView, template) {
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
                },
                error: function () {
                    //console.log('error getting used by list');
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

            this.documentsCollection.fetch({reset: true});

            return this;
        },

        bindDomElements: function () {
            this.documentsUL = this.$('#used-by-documents');
            this.partsUL = this.$('#used-by-parts');
        },

        addPartViews: function () {
            this.partsCollection.each(this.addPartView.bind(this));
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

        addDocumentView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByDocumentViews.push(usedByView);
            this.documentsUL.append(usedByView.$el);
        }

    });

    return UsedByListView;
});
