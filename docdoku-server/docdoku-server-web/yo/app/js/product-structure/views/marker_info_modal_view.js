/*global define,App*/
'use strict';
define([
        "backbone",
        "mustache",
        'text!templates/marker_info_modal.html'
    ],

    function (Backbone, Mustache, template) {

        var MarkerInfoModalView = Backbone.View.extend({

            events: {
                'click .destroy-marker-btn': 'destroyMarker',
                'hidden #markerModal': 'onHidden'
            },

            initialize: function () {
                _.bindAll(this);
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, title: this.model.getTitle()}));
                this.$modal = this.$('#markerModal');
                this.$('#markerDesc').html(this.model.getDescription().nl2br());

                return this;
            },

            destroyMarker: function () {
                if (this.model) {
                    this.model.destroy({success: function () {
                        App.collaborativeController.sendMarkersRefresh('remove marker');
                    }});
                }
                this.closeModal();
            },

            openModal: function () {
                this.$modal.modal('show');
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            }

        });

        return MarkerInfoModalView;
    }
);