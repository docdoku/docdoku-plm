/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/control_navigation.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var ControlNavigationView = Backbone.View.extend({

        className: 'side_control_group',

        events: {
            'click button#fly_to': 'flyTo',
            'click button#look_at': 'lookAt',
            'click button#reset_camera': 'resetCamera'
        },

        setObject: function (object) {
            this.$('button#look_at').removeAttr('disabled');
            this.$('button#fly_to').removeAttr('disabled');
            this.object = object;
        },

        reset: function () {
            this.$('button#look_at').attr('disabled', 'disabled');
            this.$('button#fly_to').attr('disabled', 'disabled');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.reset();
            return this;
        },

        flyTo: function () {
            App.sceneManager.flyTo(this.object);
        },

        lookAt: function () {
            App.sceneManager.lookAt(this.object);
        },

        resetCamera: function () {
            App.sceneManager.resetCameraPlace();
        }

    });

    return ControlNavigationView;
});
