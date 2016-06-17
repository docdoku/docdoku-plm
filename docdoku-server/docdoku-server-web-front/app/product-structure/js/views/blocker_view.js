/*global define,App*/
define(
    [
        'backbone',
        'mustache',
        'text!templates/blocker.html'
    ], function (Backbone, Mustache, template) {

        'use strict';

        var BlockerView = Backbone.View.extend({

            tagName: 'div',

            id: 'blocker',

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                return this;
            }

        });

        return BlockerView;

    });
