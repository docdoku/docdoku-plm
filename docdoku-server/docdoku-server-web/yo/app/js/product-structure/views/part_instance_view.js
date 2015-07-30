/*global define,App*/
define(['backbone', 'mustache', 'text!templates/part_instance.html'],
function (Backbone, Mustache, template) {
    'use strict';
    var PartInstanceView = Backbone.View.extend({

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

        setObject: function (object) {
            if (App.sceneManager.transformControlsEnabled()) {
                App.sceneManager.deleteTransformControls(this.object);
                App.sceneManager.setTransformControls(object);

            }
            this.object = object;
            return this;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {object: this.object, i18n: App.config.i18n}));
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
            App.sceneManager.flyTo(this.object);
        },

        lookAt: function () {
            App.sceneManager.lookAt(this.object);
        },

        transformView: function (e) {
            App.sceneManager.setTransformControls(this.object, e.currentTarget.id);
        },

        cancelTransformation: function () {
            App.sceneManager.cancelTransformation(this.object);
        }

    });

    return PartInstanceView;
});
