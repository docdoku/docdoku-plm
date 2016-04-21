/*global define,App*/
define([
    'backbone',
    'mustache',
    'views/shortcuts_view',
    'text!templates/control_modes.html'
], function (Backbone, Mustache, ShortcutsView, template) {
    'use strict';
    var ControlModesView = Backbone.View.extend({
        className: 'side_control_group',

        events: {
            'click button#flying_mode_view_btn': 'flyingView',
            'click button#tracking_mode_view_btn': 'trackingView',
            'click button#orbit_mode_view_btn': 'orbitView'
        },

        flyingView: function () {
            if(App.sceneManager){
                App.sceneManager.setPointerLockControls();
            }
            this.$flyingModeButton.addClass('active');
        },

        trackingView: function () {
            if(App.sceneManager){
                App.sceneManager.setTrackBallControls();
            }
            this.$trackingModeButton.addClass('active');
        },

        orbitView: function () {
            if(App.sceneManager){
                App.sceneManager.setOrbitControls();
            }
            this.$orbitModeButton.addClass('active');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.shortcutsview = new ShortcutsView().render();
            this.$('.nav-header').after(this.shortcutsview.$el);
            this.bindDomElements();
            return this;
        },

        bindDomElements: function(){
            this.$flyingModeButton = this.$('button#flying_mode_view_btn');
            this.$orbitModeButton = this.$('button#orbit_mode_view_btn');
            this.$trackingModeButton = this.$('button#tracking_mode_view_btn');
        }

    });

    return ControlModesView;

});
