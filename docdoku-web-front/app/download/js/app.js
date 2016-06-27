/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/content.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath
            })).show();
            return this;
        }
    });

    return AppView;
});
