/*global define,App */
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_details.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, AlertView) {
    'use strict';
    var ConfigurationDetailsView = Backbone.View.extend({

        events: {
        },

        template: Mustache.parse(template),

        initialize: function () {

        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: this.model}));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#configuration_details_modal');
            this.$tabBaselines = this.$('#tab-baselines');
        },

         onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
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

    return ConfigurationDetailsView;

});
