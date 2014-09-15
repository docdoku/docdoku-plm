/*global define*/
define([
    'backbone',
    "mustache",
    "text!common-objects/templates/prompt.html"
], function (Backbone, Mustache, template) {

    var PromptView = Backbone.View.extend({

        events: {
            "click #cancelPrompt": "cancelAction",
            "click #submitPrompt": "primaryAction",
            "hidden #prompt_modal": "onHidden",
            "shown #prompt_modal": "onShown"
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                title: this.title,
                question: this.question,
                primaryButton: this.primaryButton,
                cancelButton: this.cancelButton
            }));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#prompt_modal');
            this.$promptInput = this.$('#prompt_input');
        },

        primaryAction: function () {
            this.trigger("prompt-ok", [this.$promptInput.val()]);
            this.closeModal();
        },

        cancelAction: function () {
            this.trigger("prompt-cancel");
            this.closeModal();
        },

        setPromptOptions: function (title, question, primaryButton, cancelButton) {
            this.title = title;
            this.question = question;
            this.primaryButton = primaryButton;
            this.cancelButton = cancelButton;
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onShown: function () {
            this.$promptInput.focus();
        },
        onHidden: function () {
            this.remove();
        }
    });

    return PromptView;
});
