/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/part_collection',
    'collections/where_used_document',
    'views/where_used/where_used_list_item_view',
    'text!templates/where_used/where_used_list.html'
], function (Backbone, Mustache, PartList, WhereUsedDocumentList, WhereUsedListItemView, template) {
    'use strict';
    var WhereUsedListView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);

            this.partsCollection = this.options.partsCollection;
            this.documentsCollection = this.options.documentsCollection;

            this.listenTo(this.partsCollection, 'reset', this.addPartViews);
            this.listenTo(this.documentsCollection, 'reset', this.addDocumentViews);
        },

        render: function () {
            var data = {
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();

            this.whereUsedPartViews = [];
            this.whereUsedDocumentViews = [];

            //this.partsCollection.fetch({reset: true});
            this.documentsCollection.fetch({reset: true});

            return this;
        },

        bindDomElements: function () {
            this.documentsUL = this.$('#where-used-documents');
            this.partsUL = this.$('#where-used-parts');
        },

        addPartViews: function () {
            this.partsCollection.each(this.addPartView.bind(this));
        },

        addDocumentViews: function () {
            this.documentsCollection.each(this.addDocumentView.bind(this));
        },

        addPartView: function (model) {
            var whereUsedView = new WhereUsedListItemView({
                model: model
            }).render();

            this.whereUsedPartViews.push(whereUsedView);
            this.partsUL.append(whereUsedView.$el);
        },

        addDocumentView: function (model) {
            var whereUsedView = new WhereUsedListItemView({
                model: model
            }).render();

            this.whereUsedDocumentViews.push(whereUsedView);
            this.documentsUL.append(whereUsedView.$el);
        }

    });

    return WhereUsedListView;
});
