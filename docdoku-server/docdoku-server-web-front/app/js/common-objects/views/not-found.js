/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/not-found.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var NotFoundView = Backbone.View.extend({

        render: function (error, url) {
            this.$el.html(Mustache.render(template, {
                contextPath:App.config.contextPath,
                i18n: App.config.i18n,
                url:url,
                reason:error.responseText
            }));
            return this;
        }
    });

    return NotFoundView;
});
