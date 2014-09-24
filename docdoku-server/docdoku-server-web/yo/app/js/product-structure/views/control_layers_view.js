/*global define*/
define(
    [
        "backbone",
        "mustache",
        "text!templates/control_layers.html"
    ], function (Backbone, Mustache, template) {

        var ControlLayersView = Backbone.View.extend({

            className: "side_control_group",

            initialize: function () {
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                return this;
            }

        });

        return ControlLayersView;

    });