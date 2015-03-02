define([
    'backbone',
    'mustache',
    'text!common-objects/templates/lov/lov_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var LOVItemView = Backbone.View.extend({

        events:{

        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template,{
                i18n: App.config.i18n
            }));
            return this;
        }
    });

    return LOVItemView;
});
