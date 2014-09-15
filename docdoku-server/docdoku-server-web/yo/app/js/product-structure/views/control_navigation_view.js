/*global define,App*/
define(
    [
        "backbone",
        "mustache",
        "text!templates/control_navigation.html"
    ], function (Backbone, Mustache, template) {

        var PartMetadataView = Backbone.View.extend({

            className: "side_control_group",

            events: {
                "click button#fly_to": "fly_to",
                "click button#look_at": "look_at",
                "click button#reset_camera": "reset_camera"
            },

            setMesh: function (mesh) {
                this.$("button#look_at").removeAttr("disabled");
                this.$("button#fly_to").removeAttr("disabled");
                this.mesh = mesh;
            },

            reset: function () {
                this.$("button#look_at").attr("disabled", "disabled");
                this.$("button#fly_to").attr("disabled", "disabled");
            },

            render: function () {
                this.$el.html(Mustache.render(template, {mesh: this.mesh, i18n: APP_CONFIG.i18n}));
                this.reset();
                return this;
            },

            fly_to: function () {
                App.sceneManager.flyTo(this.mesh);
            },

            look_at: function () {
                App.sceneManager.lookAt(this.mesh);
            },

            reset_camera: function () {
                App.sceneManager.resetCameraPlace();
            }
        });

        return PartMetadataView;
    });