/**
 * Created with IntelliJ IDEA.
 * User: yannsergent
 * Date: 13/11/12
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */

define([
    "require",
    "i18n!localization/nls/document-management-strings",
    "models/workflow_model",
    "text!templates/workflow_model_editor.html",
    "common-objects/collections/roles",
    "common-objects/models/activity_model",
    "views/workflow_model_copy"
], function(require, i18n, WorkflowModel, template, Roles, ActivityModel, WorkflowModelCopyView) {
    var WorkflowModelEditorView = Backbone.View.extend({

        el: "#document-content",

        events: {
            "click .actions #cancel-workflow": "cancelAction",
            "click .actions #save-workflow": "saveAction",
            "click .actions #copy-workflow": "copyAction",
            "click button#add-activity": "addActivityAction"
        },

        initialize: function() {
            this.subviews = [];

            this.roles = new Roles();
            this.roles.fetch({async: false});

            if (_.isUndefined(this.options.workflowModelId)) {
                this.model = new WorkflowModel();
                this.model.attributes.activityModels.bind('add', this.addOneActivity, this);
            } else {
                this.model = new WorkflowModel({
                    id: this.options.workflowModelId
                });

                var self = this;

                this.model.fetch({success: function() {
                    self.inputFinalState.val(self.model.get('finalLifeCycleState'));
                    self.addAllActivity();
                } });
            }
        },

        addAllActivity: function() {
            this.model.attributes.activityModels.bind('add', this.addOneActivity, this);
            this.model.attributes.activityModels.each(this.addOneActivity, this);
        },

        addOneActivity: function(activityModel) {
            var self = this;
            require(["views/activity_model_editor"], function(ActivityModelEditorView) {
                var activityModelEditorView = new ActivityModelEditorView({model: activityModel, roles: self.roles});
                self.subviews.push(activityModelEditorView);
                activityModelEditorView.render();
                self.liAddActivitySection.before(activityModelEditorView.el);
            });
        },

        addActivityAction: function() {
            this.model.attributes.activityModels.add(new ActivityModel());
            return false;
        },

        activityPositionChanged: function(oldPosition, newPosition) {
            var activityModel = this.model.attributes.activityModels.at(oldPosition);
            this.model.attributes.activityModels.remove(activityModel, {silent: true});
            this.model.attributes.activityModels.add(activityModel, {silent: true, at: newPosition});
        },

        gotoWorkflows: function() {
            this.router = require("router").getInstance();
            this.router.navigate("workflows", {trigger: true});
        },

        cancelAction: function() {
            this.gotoWorkflows();
            return false;
        },

        saveAction: function() {
            var self = this;
            var reference = this.inputWorkflowName.val();

            if (reference) {
                this.model.save(
                    {
                        reference: reference,
                        finalLifeCycleState: self.inputFinalState.val()
                    },
                    {
                        success: function() {
                            self.gotoWorkflows();
                        },
                        error: function(model, xhr) {
                            console.error("Error while saving workflow '" + model.attributes.reference + "' : " + xhr.responseText);
                            self.inputWorkflowName.focus();
                        }
                    }
                );
            } else {
                this.inputWorkflowName.focus();
            }

            return false;
        },

        copyAction: function() {

            var workflowModelCopyView = new WorkflowModelCopyView({
                model: this.model
            }).render();

            $("body").append(workflowModelCopyView.el);

            return false;
        },

        render: function() {

            this.template = Mustache.render(template, {i18n: i18n, workflow: this.model.attributes});

            this.$el.html(this.template);

            this.bindDomElements();

            return this;
        },

        bindDomElements: function() {
            var self = this;

            this.inputWorkflowName = this.$("input#workflow-name");

            this.inputFinalState = this.$("input#final-state");

            this.liAddActivitySection = this.$("li#add-activity-section");

            this.activitiesUL = this.$("ul#activity-list");
            this.activitiesUL.sortable({
                items: "li.activity-section",
                handle: ".activity-topbar",
                tolerance: "pointer",
                start: function(event, ui) {
                    ui.item.oldPosition = ui.item.index();
                },
                stop: function(event, ui) {
                    self.activityPositionChanged(ui.item.oldPosition, ui.item.index());
                }
            });
        },

        unbindAllEvents: function() {
            _.each(this.subviews, function(subview) {
                subview.unbindAllEvents();
            });
            this.undelegateEvents();
        }

    });

    return WorkflowModelEditorView;
});