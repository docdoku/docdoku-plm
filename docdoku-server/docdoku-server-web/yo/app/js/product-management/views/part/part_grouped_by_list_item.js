/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_grouped_by_list_item.html'
], function (Backbone, Mustache, template){
    'use strict';
    var PartGroupedByListItemView = Backbone.View.extend({

        events: {

        },

        tagName:'tr',

        initialize: function () {
            this.item = this.options.item;
            this.headerColumns = this.options.columns;
        },

        render: function () {
            var self = this;
            var itemOrdered = [];

            _.each(this.headerColumns, function(column){
                var itemColumn = self.item[column.value];
                itemOrdered.push(itemColumn);
            });

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                columns: itemOrdered
            }));
            return this;
        }
    });

    return PartGroupedByListItemView;
});
