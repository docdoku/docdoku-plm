/*global define,App*/
define(
    [
        'backbone',
        'mustache',
        'text!templates/control_transform.html'
    ], function (Backbone, Mustache, template) {

        'use strict';

        var ControlTransformView = Backbone.View.extend({

            className: 'side_control_group',

            events: {
                'click #transform_mode_view_btn > button': 'transformView',
                'click button#cancel_transformation': 'cancelTransformation'
            },

            initialize: function () {
                this.object = undefined;
            },

            setObject: function (object) {
                if (App.sceneManager.transformControlsEnabled()) {
                    App.sceneManager.deleteTransformControls(this.object);
                    App.sceneManager.setTransformControls(object);
                }
                this.$('button').removeAttr('disabled');
                this.object = object;
                return this;
            },

            reset: function () {

                // TransformControls enabled
                if (App.sceneManager.transformControlsEnabled()) {
                    var mode = App.sceneManager.getTransformControlsMode();
                    this.$('button#' + mode).addClass('active');
                }
                else if (!this.object) {
                    this.$('button').attr('disabled', 'disabled');
                }
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                this.reset();
                return this;
            },

            transformView: function (e) {
                var modeSelected = e.currentTarget.id;

                if (modeSelected === App.sceneManager.getTransformControlsMode()) {
                    App.sceneManager.leaveTransformMode();
                } else {
                    App.sceneManager.setTransformControls(this.object, modeSelected);
                }
            },

            cancelTransformation: function () {
                App.sceneManager.cancelTransformation(this.object);
            }

        });

        return ControlTransformView;
    });
