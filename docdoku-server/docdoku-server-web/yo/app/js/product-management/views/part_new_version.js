/*global define*/
define([
    'backbone',
    "mustache",
    "text!templates/part_new_version.html",
    "common-objects/views/workflow/workflow_mapping",
    "common-objects/views/workflow/workflow_list",
    "common-objects/views/security/acl"
], function (Backbone, Mustache, template, WorkflowMappingView, WorkflowListView, ACLView) {
    var PartNewVersionView = Backbone.View.extend({

        id: "new-version-modal",
        className: "modal hide fade",

        events: {
            "click #create-new-version-btn": "createNewVersionAction",
            "click #cancel-new-version-btn": "closeModalAction",
            "click a.close": "closeModalAction"
        },

        initialize: function () {

        },

        render: function () {

            this.template = Mustache.render(template, {i18n: APP_CONFIG.i18n, model: this.model});

            this.$el.html(this.template);

            this.$el.modal("show");

            this.bindDomElements();

            this.workflowsView = new WorkflowListView();
            this.newVersionWorkflowDiv.html(this.workflowsView.el);

            this.workflowsMappingView = new WorkflowMappingView({
                el: this.$("#workflows-mapping")
            });

            this.workflowsView.on("workflow:change", this.workflowsMappingView.updateMapping);

            this.aclView = new ACLView({
                el: this.$("#acl-mapping"),
                editMode: true
            }).render();

            this.$(".tabs").tabs();

            return this;
        },

        bindDomElements: function () {
            this.newVersionWorkflowDiv = this.$("#new-version-workflow");
            this.textAreaNewVersionDescription = this.$("#new-version-description");
        },

        createNewVersionAction: function () {
            this.model.createNewVersion(this.textAreaNewVersionDescription.val(), this.workflowsView.selected(), this.workflowsMappingView.toList(), this.aclView.toList());
            this.closeModalAction();
        },

        closeModalAction: function () {
            this.$el.modal("hide");
            this.remove();
        }

    });

    return PartNewVersionView;

});
