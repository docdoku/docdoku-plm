/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/used_by_list_item_part.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var UsedByListItemPartView = Backbone.View.extend({

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

            debugger
            this.$el.html(Mustache.render(template, data));
            return this;
        }

    });

    return UsedByListItemPartView;
});
