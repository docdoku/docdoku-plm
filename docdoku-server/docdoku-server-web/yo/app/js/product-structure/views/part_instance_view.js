/*global define,App*/
define(['backbone', 'mustache', 'text!templates/part_instance.html'],
function (Backbone, Mustache, template) {
    'use strict';
    var PartMetadataView = Backbone.View.extend({

        tagName: 'div',

        id: 'part_instance_container',

        events: {
            'click button#fly_to': 'flyTo',
            'click button#look_at': 'lookAt',
            'click #transform_mode_view_btn > button': 'transformView',
            'click button#cancel_transformation': 'cancelTransformation'
        },

        className: 'side_control_group',

        initialize: function () {

        },

        setMesh: function (mesh) {
            if (App.sceneManager.transformControlsEnabled()) {
                App.sceneManager.deleteTransformControls(this.mesh);
                App.sceneManager.setTransformControls(mesh);

            }
            this.mesh = mesh;
            return this;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {mesh: this.mesh, i18n: App.config.i18n}));
            if (App.sceneManager.transformControlsEnabled()) {
                var mode = App.sceneManager.getTransformControlsMode();
                this.$('button#' + mode).addClass('active');

            }
            return this;
        },

        reset: function () {
            if (! App.sceneManager.transformControlsEnabled()) {
                this.$el.empty();
            }
        },

        flyTo: function () {
            App.sceneManager.flyTo(this.mesh);
        },

        lookAt: function () {
            App.sceneManager.lookAt(this.mesh);
        },

        transformView: function (e) {
            App.sceneManager.setTransformControls(this.mesh, e.currentTarget.id);
        },

        cancelTransformation: function () {
            //$('#transform_mode_view_btn').removeClass('active');
            App.sceneManager.cancelTransformation(this.mesh);
        }

    });

    return PartMetadataView;
});
