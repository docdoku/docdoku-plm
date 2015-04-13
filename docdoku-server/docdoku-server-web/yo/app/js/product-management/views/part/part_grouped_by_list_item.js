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

        initialize: function () {
            this.item = this.options.item;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                columns: _.values(this.item)
            }));
            return this;
        }
    });

    return PartGroupedByListItemView;
});
