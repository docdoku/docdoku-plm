/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/document/used_by_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var UsedByListItemView = Backbone.View.extend({

        tagName: 'li',
        className: 'used-by-item well',

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

    return UsedByListItemView;
});

