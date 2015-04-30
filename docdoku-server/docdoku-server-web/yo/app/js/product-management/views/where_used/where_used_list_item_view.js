/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    //'common-objects/models/part',
    //'models/document',
    'text!templates/where_used/where_used_list_item.html'
], function (Backbone, Mustache, /*Part, Document, */template) {
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

