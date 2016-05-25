/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/permalink.html'
], function (Backbone, Mustache, template) {
	'use strict';

    var AppView = Backbone.View.extend({

        el: '#content',

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            })).show();
            return this;
        }

    });

    return AppView;
});
