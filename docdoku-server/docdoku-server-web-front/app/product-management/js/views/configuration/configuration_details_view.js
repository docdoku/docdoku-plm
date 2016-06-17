/*global _,define,App */
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_details.html',
    'text!common-objects/templates/path/path.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, pathTemplate, AlertView) {
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
            var substitutes = this.model.getSubstitutesParts();
            var optionals = this.model.getOptionalsParts();
            this.$substitutesCount.text(substitutes.length);
            this.$optionalsCount.text(optionals.length);

            _.each(substitutes,this.drawSubstitutesChoice.bind(this));
            _.each(optionals,this.drawOptionalsChoice.bind(this));
        },

        drawSubstitutesChoice:function(data){
            this.$substitutes.append(Mustache.render(pathTemplate, {i18n: App.config.i18n, partLinks: data.partLinks}));
            this.$substitutes.find('.well i.fa-long-arrow-right').last().remove();
        },

        drawOptionalsChoice:function(data){
            this.$optionals.append(Mustache.render(pathTemplate, {i18n: App.config.i18n, partLinks: data.partLinks}));
            this.$optionals.find('.well i.fa-long-arrow-right').last().remove();
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#configuration_details_modal');
            this.$substitutes = this.$('.substitutes-list');
            this.$substitutesCount = this.$('.substitutes-count');
            this.$optionals = this.$('.optionals-list');
            this.$optionalsCount = this.$('.optionals-count');
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
