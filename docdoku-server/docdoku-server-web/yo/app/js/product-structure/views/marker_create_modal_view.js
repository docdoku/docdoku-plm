/*global define,App,_*/
define([
        'backbone',
        'mustache',
        'text!templates/marker_create_modal.html'
    ],

    function (Backbone, Mustache, template) {

        'use strict';

        var MarkerCreateModalView = Backbone.View.extend({

            events: {
                'submit form#save-marker': 'saveMarker',
                'hidden #creationMarkersModal': 'onHidden'
            },

            initialize: function () {
                _.bindAll(this);
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                this.$modal = this.$('#creationMarkersModal');
                this.$markersModalInputName = this.$('input[name=makerName]');
                this.$markersModalInputDescription = this.$('textarea');

                return this;
            },

            saveMarker: function (e) {
                this.model.createMarker(this.$markersModalInputName.val(), this.$markersModalInputDescription.val(), this.options.intersectPoint.x, this.options.intersectPoint.y, this.options.intersectPoint.z);
                this.closeModal();
                e.preventDefault();
                return false;
            },

            openModal: function () {
                this.$modal.modal('show');
                this.$markersModalInputName.focus();
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            }

        });

        return MarkerCreateModalView;
    }
);
