/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/control_options.html'
], function (Backbone, Mustache, template) {
    'use strict';

    var ControlOptionsView = Backbone.View.extend({
        className: 'side_control_group',

        events: {
            'click button#gridSwitch': 'gridSwitch',
            'click button#screenshot': 'takeScreenShot',
            'click button#show_edited_meshes': 'showEditedMeshes'
        },

        gridSwitch: function () {
            var gridSwitch = this.$('#gridSwitch');
            gridSwitch.toggleClass('active');
            App.SceneOptions.grid = !!gridSwitch.hasClass('active');
        },

        takeScreenShot: function () {
            App.sceneManager.takeScreenShot();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            return this;
        },

        showEditedMeshes: function () {
            var showEditedMeshes = this.$('#show_edited_meshes');
            showEditedMeshes.toggleClass('active');
            if (showEditedMeshes.hasClass('active')) {
                App.sceneManager.colourEditedObjects();
            } else {
                App.sceneManager.cancelColourEditedObjects();
            }
        }


    });

    return ControlOptionsView;
});
