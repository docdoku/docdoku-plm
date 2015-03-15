/*global define,App*/
define(
    [
        'backbone',
        'mustache',
        'text!templates/control_clipping.html'
    ],
    function (Backbone, Mustache, template) {

        'use strict';

        var ControlOptionsView = Backbone.View.extend({

            className: 'side_control_group',

            events: {
                'input input#slider-clipping': 'clipping'
            },

            initialize: function () {
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                return this;
            },

            clipping: function (e) {
                var value = e.target.value;
                App.collaborativeController.sendClippingValue(value);
                // I remove the clipping for the last quarter of the scene to be more accurate
                var max = App.SceneOptions.cameraFar * 3 / 4;
                var percentage = value * Math.log(max) / 100; // cross product to set a value to pass to the exponential function
                App.sceneManager.setCameraNear(Math.exp(percentage));
            }

        });

        return ControlOptionsView;

    });
