/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/where_used/where_used_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var WhereUsedListItemView = Backbone.View.extend({

        tagName: 'li',
        className: 'where-used-item well',

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            var data = {
                i18n: App.config.i18n,
                model: this.model
            };

            this.$el.html(Mustache.render(template, data));
            return this;
        }

    });

    return WhereUsedListItemView;
});

