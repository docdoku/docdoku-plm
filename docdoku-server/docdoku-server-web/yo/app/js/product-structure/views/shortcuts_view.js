/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/shortcuts.html',
    'views/controls_infos_modal_view'
], function (Backbone, Mustache, template, ControlsInfosModalView) {

    'use strict';

    var ShortcutsView = Backbone.View.extend({

        tagName: 'div',

        id: 'shortcuts',

        events: {
            'click a': 'clicked'
        },

        initialize: function () {

        },

        clicked: function () {
            var cimv;
            switch (App.sceneManager.stateControl) {
                case App.sceneManager.STATECONTROL.PLC:
                    cimv = new ControlsInfosModalView({isPLC: true, isTBC: false, isORB: false});
                    break;
                case App.sceneManager.STATECONTROL.TBC:
                    cimv = new ControlsInfosModalView({isPLC: false, isTBC: true, isORB: false});
                    break;
                case App.sceneManager.STATECONTROL.ORB:
                    cimv = new ControlsInfosModalView({isPLC: false, isTBC: false, isORB: true});
                    break;
            }
            window.document.body.appendChild(cimv.render().el);
            cimv.openModal();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            return this;
        }

    });

    return ShortcutsView;

});
