/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/content.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        events: {
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            })).show();
            return this;
        },

        editAccount:function(){
        }

    });

    return AppView;
});
