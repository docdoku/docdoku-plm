define([
    "common-objects/views/workflow/lifecycle_activity",
    "i18n!localization/nls/document-management-strings",
    "text!common-objects/templates/workflow/lifecycle.html",
    "common-objects/utils/date"

], function(LifecycleActivityView, i18n, template, Date) {

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

                    _.each(abortedWorkflows,function(workflow){

                        workflow.abortedFormattedDate = Date.formatTimestamp(
                            i18n._DATE_FORMAT,
                            workflow.abortedDate
                        );
                    });

                    // Find last rejected tasks in aborted workflows...
                    // Last aborted task will replace corresponding task in current workflow
                    if(abortedWorkflows.length){
                        for(var i = 0 ; i < that.workflow.activities.length; i++){
                            if(i >= that.workflow.currentStep){
                                for(var j = 0 ; j < that.workflow.activities[i].tasks.length; j++){
                                    if(that.workflow.activities[i].tasks[j].status == "NOT_STARTED"){
                                        for(var k = abortedWorkflows.length - 1 ; k -- ; k >= 0){
                                           if(abortedWorkflows[k].activities[i].tasks[j].status == "REJECTED"){
                                               that.workflow.activities[i].tasks[j] = abortedWorkflows[k].activities[i].tasks[j];
                                               break;
                                           }
                                        }
                                    }
                                }
                            }
                        }
                    }

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

        currentWorkflow:function(e){
            this.displayWorkflow(this.workflow);
            this.$historyContent.find("a.active").removeClass("active");
            $(e.target).addClass("active");
        },

        abortedWorkflow:function(e){
            var that = this;
            var workflowId = $(e.target).data("id");
            var workflow = _.select(that.abortedWorkflows,function(workflow){return workflow.id == workflowId})[0];
            if(workflow){
                this.$historyContent.find("a.active").removeClass("active");
                $(e.target).addClass("active");
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