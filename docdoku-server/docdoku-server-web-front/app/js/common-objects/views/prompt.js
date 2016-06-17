/*global _,define*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/prompt.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var PromptView = Backbone.View.extend({

        events: {
            'click #cancelPrompt': 'cancelAction',
            'click #submitPrompt': 'primaryAction',
            'hidden #prompt_modal': 'onHidden',
            'shown #prompt_modal': 'onShown'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                title: this.title,
                question: this.question,
                primaryButton: this.primaryButton,
                cancelButton: this.cancelButton,
                inputSpecified: this.inputSpecified,
                defaultValue: this.defaultValue,
                label: this.label,
                type:this.type || 'text'
            }));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#prompt_modal');
            this.$promptInput = this.$('#prompt_input');
        },

        primaryAction: function (e) {
            this.trigger('prompt-ok', [this.$promptInput.val()]);
            this.closeModal();
            e.preventDefault();
            return false;
        },

        cancelAction: function (e) {
            this.trigger('prompt-cancel');
            this.closeModal();
            e.preventDefault();
            return false;
        },

        setPromptOptions: function (title, question, primaryButton, cancelButton, defaultValue, label, type) {
            this.title = title;
            this.question = question;
            this.primaryButton = primaryButton;
            this.cancelButton = cancelButton;
            this.defaultValue = defaultValue;
            this.label = label;
            this.type = type;
        },

        specifyInput: function(inputSpecified) {
            this.inputSpecified = inputSpecified;
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onShown: function () {
            this.$promptInput.focus();
            this.$promptInput.addClass('ready');
        },

        onHidden: function () {
            this.remove();
        }
    });

    return PromptView;
});
