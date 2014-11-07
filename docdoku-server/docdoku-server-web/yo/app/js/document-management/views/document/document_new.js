/*global define*/
define([
    "mustache",
    "common-objects/views/components/modal",
    "common-objects/views/attributes/attributes",
    "views/document/document_template_list",
    "common-objects/views/workflow/workflow_list",
    "common-objects/views/workflow/workflow_mapping",
    "common-objects/views/security/acl",
    "text!templates/document/document_new.html"
], function (Mustache, ModalView, AttributesView, DocumentTemplateListView, DocumentWorkflowListView, DocumentWorkflowMappingView, ACLView, template) {
    var DocumentNewView = ModalView.extend({

        template: template,

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events["submit #form-" + this.cid] = "onSubmitForm";
        },

        rendered: function () {

            this.attributesView = this.addSubView(
                new AttributesView({
                    el: "#tab-attributes-" + this.cid
                })
            );

            this.attributesView.render();

            this.templatesView = this.addSubView(
                new DocumentTemplateListView({
                    el: "#templates-" + this.cid,
                    attributesView: this.attributesView
                })
            );

            this.templatesView.collection.fetch({reset: true});

            this.workflowsView = this.addSubView(
                new DocumentWorkflowListView({
                    el: "#workflows-" + this.cid
                })
            );

            this.workflowsMappingView = this.addSubView(
                new DocumentWorkflowMappingView({
                    el: "#workflows-mapping-" + this.cid
                })
            );

            this.workflowsView.on("workflow:change", this.workflowsMappingView.updateMapping);

            this.workspaceMembershipsView = new ACLView({
                el: this.$("#acl-mapping-" + this.cid),
                editMode: true
            }).render();
        },

        onSubmitForm: function () {

            var reference = $("#form-" + this.cid + " .reference").val();

            if (reference) {
                var workflow = this.workflowsView.selected();
                var template = this.templatesView.selected();
                var acl = this.workspaceMembershipsView.toList();

                var data = {
                    reference: reference,
                    title: $("#form-" + this.cid + " .title").val(),
                    description: $("#form-" + this.cid + " .description").val(),
                    workflowModelId: workflow ? workflow.get("id") : null,
                    templateId: template ? template.get("id") : null,
                    roleMapping: workflow ? this.workflowsMappingView.toList() : null,
                    acl: acl
                };

                this.collection.create(data, {
                    success: this.success,
                    error: this.error,
                    wait: true
                });
            }

            return false;
        },

        success: function (model, response) {
            var that = this;
            model.getLastIteration().save({
                instanceAttributes: this.attributesView.collection.toJSON()
            }, {
                success: function () {
                    that.hide();
                    model.fetch();
                },
                error: this.error
            });
        },

        error: function (model, error) {
            this.collection.remove(model);
            if (error.responseText) {
                this.alert({
                    type: "error",
                    message: error.responseText
                });
            } else {
                console.error(error);
            }
        }

    });

    return DocumentNewView;

});
