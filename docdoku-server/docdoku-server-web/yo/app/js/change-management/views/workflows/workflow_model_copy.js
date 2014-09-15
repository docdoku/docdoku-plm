define([
    'backbone',
    "mustache",
    "require",
    "text!templates/workflows/workflow_model_copy.html"
], function (Backbone, Mustache, require, template) {
    var WorkflowModelCopyView = Backbone.View.extend({

        id: "modal-copy-workflow",
        className: "modal hide fade",

        events: {
            "click #save-copy-workflow-btn": "saveCopyAction",
            "click #cancel-copy-workflow-btn": "closeModalAction",
            "click a.close": "closeModalAction"
        },

        initialize: function () {

        },

        render: function () {

            this.template = Mustache.render(template, {i18n: APP_CONFIG.i18n, workflow: this.model.attributes});

            this.$el.html(this.template);

            this.$el.modal("show");

            this.bindDomElements();

            return this;
        },

        bindDomElements: function () {
            this.inputWorkflowCopyName = this.$("#workflow-copy-name");
        },

        saveCopyAction: function () {
            var self = this;
            var reference = this.inputWorkflowCopyName.val();

            if (reference != null && reference != "") {
                delete this.model.id;
                this.model.save(
                    {
                        reference: reference,
                        finalLifeCycleState: self.model.get('finalLifeCycleState')
                    },
                    {
                        success: function () {
                            self.closeModalAction();
                            self.gotoWorkflows();
                        },
                        error: function (model, xhr) {
                            console.error("Error while saving workflow '" + model.attributes.reference + "' : " + xhr.responseText);
                            self.inputWorkflowCopyName.focus();
                        }
                    }
                );
            }
        },

        closeModalAction: function () {
            this.$el.modal("hide");
            this.remove();
        },

        gotoWorkflows: function () {
            App.router.navigate(APP_CONFIG.workspaceId + "/workflows", {trigger: true});
        }

    });

    return WorkflowModelCopyView;

});
