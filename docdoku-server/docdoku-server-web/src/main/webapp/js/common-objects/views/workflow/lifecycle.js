define([
    "common-objects/views/workflow/lifecycle_activity",
    "i18n!localization/nls/document-management-strings",
    "text!common-objects/templates/workflow/lifecycle.html"

], function(LifecycleActivityView, i18n, template) {

    var LifecycleView = Backbone.View.extend({

        tagName: 'div',

        events: {
            "click a.history":"history",
            "click a.abortedWorkflow":"abortedWorkflow",
            "click a.currentWorkflow":"currentWorkflow"
        },

        initialize: function() {
        },

        setAbortedWorkflowsUrl:function(url){
            this.abortedWorkflowsUrl = url;
            return this;
        },

        setWorkflow:function(workflow){
            this.workflow = workflow;
            return this;
        },

        setEntityType:function(entityType){
            this.entityType=entityType;
            return this;
        },

        render: function() {

            var that = this ;
            $.ajax({
                url:this.abortedWorkflowsUrl,
                success:function(abortedWorkflows){
                    that.abortedWorkflows = abortedWorkflows;
                    that.$el.html(Mustache.render(template, {i18n: i18n, workflow:that.workflow, abortedWorkflows : abortedWorkflows}));
                    that.bindDomElements();
                    that.displayWorkflow(that.workflow);
                }
            });
            return this;
        },

        bindDomElements: function() {
            this.$historyContent = this.$(".history-content");
            this.$lifecycleActivities = this.$("#lifecycle-activities");
        },

        history:function(){
            this.$historyContent.toggleClass("hide");
        },

        currentWorkflow:function(){
            this.displayWorkflow(this.workflow);
        },

        abortedWorkflow:function(e){
            var that = this;
            var workflowId = $(e.target).data("id");
            var workflow = _.select(that.abortedWorkflows,function(workflow){console.log("compare " + workflow.id + " with " + workflowId);return workflow.id == workflowId})[0];
            if(workflow){
                this.displayWorkflow(workflow);
            }
        },

        displayWorkflow:function(workflow){
            var that = this;
            this.$lifecycleActivities.empty();
            _.each(workflow.activities,function(activity){
                activity.parentWorkflowId = that.workflow.id;
                var lifecycleActivityView = new LifecycleActivityView().setActivity(activity).setEntityType(that.entityType).render();
                that.$lifecycleActivities.append(lifecycleActivityView.$el);
                lifecycleActivityView.on("activity:change",function(){
                    that.trigger("lifecycle:change");
                });
            });
        }

    });
    return LifecycleView;
});