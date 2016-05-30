/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/document-revision.html',
], function (Backbone, Mustache, template) {
    'use strict';

    var DocumentRevisionView = Backbone.View.extend({
        render: function (document) {

            var lastIteration = document.documentIterations[document.documentIterations.length-1];

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath,
                document:document,
                lastIteration:lastIteration
            })).show();
            return this;
        }
    });

    return DocumentRevisionView;
});
