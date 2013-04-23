define([
    "views/document/document_lifecycle_activity",
    "i18n!localization/nls/document-management-strings",
    "text!templates/document/document_lifecycle.html"

], function(LifecycleActivityView, i18n, template) {

    var LifecycleDocumentView = Backbone.View.extend({

        tagName: 'div',

        events: {
        },

        initialize: function() {
        },

        setWorkflow:function(workflow){
            this.workflow = workflow;
            return this;
        },

        render: function() {

            var that = this ;
            this.$el.html(Mustache.render(template, {i18n: i18n, workflow:this.workflow}));

            var $lifecycleActivities = this.$("#lifecycle-activities");

            _.each(this.workflow.activities,function(activity){
                activity.parentWorkflowId = that.workflow.id;

                var lifecycleActivityView = new LifecycleActivityView().setActivity(activity).render();
                $lifecycleActivities.append(lifecycleActivityView.$el);
                lifecycleActivityView.on("activity:change",function(){
                    that.trigger("lifecycle:change");
                });
            });

            this.bindDomElements();
            return this;
        },

        bindDomElements: function() {
        }

    });
    return LifecycleDocumentView;
});