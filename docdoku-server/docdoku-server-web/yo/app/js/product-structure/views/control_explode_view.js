/*global define,App*/
define(
    [
        'backbone',
        'mustache',
        'text!templates/control_explode.html'
    ],
    function (Backbone, Mustache, template) {

        'use strict';

        var ControlOptionsView = Backbone.View.extend({

            className: 'side_control_group',

            events: {
                'input input#slider-explode': 'explode'
            },

            initialize: function () {
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                return this;
            },

            explode: function (e) {
                App.sceneManager.explodeScene(e.target.value);
            }

        });

        return ControlOptionsView;

    });
