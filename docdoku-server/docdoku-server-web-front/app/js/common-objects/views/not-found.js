/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/not-found.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var NotFoundView = Backbone.View.extend({

        render: function () {
            this.$el.html(Mustache.render(template, {
                url:'todo'
            }));
            return this;
        }
    });

    return NotFoundView;
});
