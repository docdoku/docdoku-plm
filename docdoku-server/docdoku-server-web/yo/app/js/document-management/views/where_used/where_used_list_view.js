/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/part_collection',
    'collections/where_used_document',
    'text!templates/where_used/where_used_list.html'
], function (Backbone, Mustache, PartList, WhereUsedDocumentList, template) {
    'use strict';
    var WhereUsedListView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            var data = {
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));

            this.whereUsedViews = [];

            //this.partsCollection.each(this.addView.bind(this));
            //this.documentsCollection.each(this.addView.bind(this));

            return this;
        },

        addView: function (model) {
            var whereUsedView = new WhereUsedListItemView({
                model: model
            }).render();

            this.whereUsedViews.push(whereUsedView);
            this.$el.append(whereUsedView.$el);
        }

    });

    return WhereUsedListView;
});
