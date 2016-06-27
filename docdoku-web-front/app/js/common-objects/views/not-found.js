/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/not-found.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var NotFoundView = Backbone.View.extend({

        render: function (error, url) {

            var tmpContainer = document.createElement('div');
            var text = document.createTextNode(error.responseText);
            tmpContainer.appendChild(text);
            text =  tmpContainer.innerHTML;

            this.$el.html(Mustache.render(template, {
                contextPath:App.config.contextPath,
                i18n: App.config.i18n,
                url:url,
                reason:text
            }));

            return this;
        }
    });

    return NotFoundView;
});
