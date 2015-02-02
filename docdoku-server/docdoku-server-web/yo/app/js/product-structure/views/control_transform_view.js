/*global define,App*/
'use strict';
define(
    [
        "backbone",
        "mustache",
        "text!templates/control_transform.html"
    ], function (Backbone, Mustache, template) {

        var ControlTransformView = Backbone.View.extend({

            className: "side_control_group",

            events: {
                "click #transform_mode_view_btn > button": "transformView",
                "click button#cancel_transformation": "cancelTransformation"
            },

            initialize: function () {
                this.mesh = undefined;
            },

            setMesh: function (mesh) {
                if (App.sceneManager.transformControlsEnabled()) {
                    App.sceneManager.deleteTransformControls(this.mesh);
                    App.sceneManager.setTransformControls(mesh);
                }
                this.$("button").removeAttr("disabled");
                this.mesh = mesh;
                return this;
            },

            reset: function () {

                // TransformControls enabled
                if (App.sceneManager.transformControlsEnabled()) {
                    var mode = App.sceneManager.getTransformControlsMode();
                    this.$("button#" + mode).addClass("active");
                } // A mesh is selected
                else if (!this.mesh) {
                    this.$("button").attr("disabled", "disabled");
                }
            },

            render: function () {
                this.$el.html(Mustache.render(template, {mesh: this.mesh, i18n: App.config.i18n}));
                this.reset();
                return this;
            },

            transformView: function (e) {
                var modeSelected = e.currentTarget.id;

                if (modeSelected === App.sceneManager.getTransformControlsMode()) {
                    App.sceneManager.leaveTransformMode();
                } else {
                    App.sceneManager.setTransformControls(this.mesh, modeSelected);
                }
            },

            cancelTransformation: function () {
                //$('#transform_mode_view_btn').removeClass("active");
                App.sceneManager.cancelTransformation(this.mesh);
            }

        });

        return ControlTransformView;
    });
