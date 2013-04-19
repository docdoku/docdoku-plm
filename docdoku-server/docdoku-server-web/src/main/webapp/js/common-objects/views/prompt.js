define([
    "text!common-objects/templates/prompt.html"
], function(template, ModalView) {

    var PromptView = Backbone.View.extend({

        template: Mustache.compile(template),

        events: {
            "click #cancelPrompt": "cancelAction",
            "click #submitPrompt": "primaryAction",
            "hidden #prompt_modal": "onHidden"
        },

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {
            this.$el.html(this.template({
                title: this.title,
                question: this.question,
                primaryButton: this.primaryButton,
                cancelButton: this.cancelButton
            }));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function() {
            this.$modal = this.$('#prompt_modal');
            this.$promptInput = this.$('#prompt_input');
        },

        primaryAction: function() {
            this.trigger("prompt-ok", [this.$promptInput.val()]);
            this.closeModal();
        },

        cancelAction: function() {
            this.trigger("prompt-cancel");
            this.closeModal();
        },

        setPromptOptions: function(title, question, primaryButton, cancelButton) {
            this.title = title;
            this.question = question;
            this.primaryButton = primaryButton;
            this.cancelButton = cancelButton;
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        }
    });

    return PromptView;
});
