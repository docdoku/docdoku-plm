/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!document-management/templates/where_used/where_used_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var WhereUsedListItemView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            var data = {
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            return this;
        }

    });

    return WhereUsedListItemView;
});

