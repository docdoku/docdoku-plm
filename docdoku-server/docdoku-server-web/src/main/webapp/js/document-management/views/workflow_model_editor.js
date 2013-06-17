define([
    "require",
    "i18n!localization/nls/document-management-strings",
    "common-objects/models/workflow_model",
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
            var that = this ;
            this.subviews = [];
            this.roles = new Roles();
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
            _.each(this.subviews,function(subview){
                subview.trigger("activities-order:changed");
            })
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

            var that = this ;

            this.roles.fetch({reset:true,success:function(){

                if (_.isUndefined(that.options.workflowModelId)) {
                    that.model = new WorkflowModel();
                    that.model.attributes.activityModels.bind('add', that.addOneActivity, that);
                } else {
                    that.model = new WorkflowModel({
                        id: that.options.workflowModelId
                    });

                    that.model.fetch({success: function() {
                        that.inputFinalState.val(that.model.get('finalLifeCycleState'));
                        that.addAllActivity();
                    } });
                }

                that.template = Mustache.render(template, {i18n: i18n, workflow: that.model.attributes});

                that.$el.html(that.template);

                that.bindDomElements();

            }});

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
                    console.log("start, index = " + ui.item.oldPosition)
                },
                stop: function(event, ui) {
                    self.activityPositionChanged(ui.item.oldPosition-1, ui.item.index()-1);
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