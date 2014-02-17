define([
    "require",
    "i18n!localization/nls/document-management-strings",
    "text!templates/workflow_model_copy.html"
], function (
    require,
    i18n,
    template
    ) {
    var WorkflowModelCopyView = Backbone.View.extend({

        id: "modal-copy-workflow",
        className: "modal hide fade",

        events: {
            "click #save-copy-workflow-btn":    "saveCopyAction",
            "click #cancel-copy-workflow-btn":  "closeModalAction",
            "click a.close":                    "closeModalAction"
        },

        initialize: function() {

        },

        render: function() {

            this.template = Mustache.render(template, {i18n: i18n, workflow: this.model.attributes});

            this.$el.html(this.template);

            this.$el.modal("show");

            this.bindDomElements();

            return this;
        },

        bindDomElements: function() {
            this.inputWorkflowCopyName = this.$("#workflow-copy-name");
            this.inputFinalState = $("input#final-state");
        },

        saveCopyAction: function() {
            var self = this;
            var reference = this.inputWorkflowCopyName.val();

            if (reference != null && reference != "") {
                delete this.model.id;
                this.model.save(
                    {
                        reference: reference,
                        finalLifeCycleState: self.inputFinalState.val()
                    },
                    {
                        success: function() {
                            self.closeModalAction();
                            self.gotoWorkflows();
                        },
                        error: function(model, xhr) {
                            console.error("Error while saving workflow '" + model.attributes.reference + "' : " + xhr.responseText);
                            self.inputWorkflowCopyName.focus();
                        }
                    }
                );
            }
        },

        closeModalAction: function() {
            this.$el.modal("hide");
            this.remove();
        },

        gotoWorkflows: function() {
            this.router = require("router").getInstance();
            this.router.navigate("workflows", {trigger: true});
        }

    });

    return WorkflowModelCopyView;

});
