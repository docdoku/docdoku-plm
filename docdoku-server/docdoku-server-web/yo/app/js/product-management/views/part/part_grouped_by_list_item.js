/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_grouped_by_list_item.html',
    '../../../utils/query-builder-options',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, querybuilderOptions, Date){
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

                var value = self.item[column.value];
                var isDate = _.findWhere(querybuilderOptions.filters, {id : column.value}).type === 'date';

                if(isDate) {
                    if (!value) {
                        value = "";
                    }
                    var timestampFormated = Date.formatTimestamp(
                        App.config.i18n._DATE_FORMAT,
                        value
                    );

                    value = timestampFormated ? timestampFormated : "";
                }

                var itemColumn = {
                    isDate : isDate,
                    value : value
                };
                itemOrdered.push(itemColumn);
            });

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                columns: itemOrdered
            }));

            Date.dateHelper(this.$('.date-popover'));
            return this;
        }
    });

    return PartGroupedByListItemView;
});
