/*global define,App */
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_details.html',
    'text!templates/configuration/configuration_choice.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, choiceTemplate, AlertView) {
    'use strict';
    var ConfigurationDetailsView = Backbone.View.extend({

        events: {
            'hidden #configuration_details_modal': 'onHidden'
        },


        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: this.model}));
            this.bindDomElements();
            this.renderChoices();
            return this;
        },

        renderChoices:function(){
            _.each(this.model.getSubstitutesParts(),this.drawSubstitutesChoice.bind(this));
            _.each(this.model.getOptionalsParts(),this.drawOptionalsChoice.bind(this));

        },

        drawSubstitutesChoice:function(data){
            this.$substitutes.append(Mustache.render(choiceTemplate, {i18n: App.config.i18n, data: {
                parts:data.parts,
                concernedPart:data.parts.pop()
            }}));
            this.$substitutes.find('i.fa-chevron-right:last-child').remove();
        },

        drawOptionalsChoice:function(data){
            this.$optionals.append(Mustache.render(choiceTemplate, {i18n: App.config.i18n, data: {
                parts:data.parts,
                concernedPart:data.parts.pop()
            }}));
            this.$optionals.find('i.fa-chevron-right:last-child').remove();
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#configuration_details_modal');
            this.$substitutes = this.$('.substitutes-list');
            this.$optionals = this.$('.optionals-list');
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
