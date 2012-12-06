/**
 * Created with IntelliJ IDEA.
 * User: yannsergent
 * Date: 13/11/12
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */

define([
    "require",
    "i18n",
    "models/workflow_model",
    "text!templates/workflow_model_editor.html",
    "models/activity_model",
    "views/activity_model_editor"

], function (
    require,
    i18n,
    WorkflowModel,
    template,
    ActivityModel,
    ActivityModelEditorView
    ) {
    var WorkflowModelEditorView = Backbone.View.extend({
        template: Mustache.render(template, {i18n: i18n}),

        el: "#content",

        events: {
            "click .actions .cancel" : "cancelAction",
            "click .actions .btn-primary" : "saveAction",
            "click button#add-activity" : "addActivityAction"
        },

        initialize: function() {
            this.model = new WorkflowModel();
            this.model.attributes.activityModels.bind('add', this.addOneActivity, this);
            this.model.attributes.activityModels.bind('reset', this.addAllActivity, this);
            //this.model.activityList.fetch();
        },

        addAllActivity: function() {
            this.collection.each(this.addOne, this);
        },

        addOneActivity: function(activityModel) {
            var activityModelEditorView = new ActivityModelEditorView({model: activityModel});
            activityModelEditorView.render();
            this.activitiesUL.append(activityModelEditorView.el);
        },

        addActivityAction: function(){
            this.model.attributes.activityModels.add(new ActivityModel());
            return false;
        },

        gotoWorkflows: function() {
            this.undelegateEvents();
            this.router = require("router").getInstance();
            this.router.navigate("workflows", {trigger: true});
        },

        cancelAction: function () {
            this.gotoWorkflows();
            return false;
        },

        saveAction: function () {
            var self = this;
            var reference = this.$el.find("input.name").first().val();

            if (reference) {
                this.model.save(
                    {
                        reference: reference
                    },
                    {
                        success: function(){
                            self.gotoWorkflows();
                        },
                        error: function(model, xhr){
                            console.error("Error while saving workflow '" + model.attributes.reference + "' : " + xhr.responseText);
                        }
                    }
                );
            }
            return false
        },

        render: function() {
            this.$el.html(this.template);
            this.activitiesUL = this.$el.find("ul#activity-list");
            return this;
        }

    });

    return WorkflowModelEditorView;
});