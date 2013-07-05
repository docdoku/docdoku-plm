define([
    "common-objects/views/workflow/lifecycle_task",
    "i18n!localization/nls/document-management-strings",
    "text!common-objects/templates/workflow/lifecycle_activity.html"

], function(LifecycleTaskView, i18n, template) {

    var LifecycleActivityView = Backbone.View.extend({

        tagName: 'div',
        className:'activity well',

        events: {
        },

        initialize: function() {
        },

        setActivity:function(activity){
            this.activity = activity;
            return this;
        },

        setEntityType:function(entityType){
            this.entityType=entityType;
            return this;
        },

        render: function() {

            var that = this ;

            this.$el.html(Mustache.render(template, {i18n: i18n, activity:this.activity}));

            var completeClass = this.activity.complete ? "complete" : "incomplete";

            this.$el.addClass(this.activity.type.toLowerCase()).addClass(completeClass);

            var $tasks = this.$(".tasks");

            _.each(this.activity.tasks,function(task, index){

                task.parentWorkflowId = that.activity.parentWorkflowId;
                task.parentActivityStep = that.activity.step;
                task.index = index;

                var lifecycleTaskView = new LifecycleTaskView().setTask(task).setEntityType(that.entityType).render();

                $tasks.append(lifecycleTaskView.$el);
                lifecycleTaskView.on("task:change",function(){
                    that.trigger("activity:change");
                });

            });

            return this;

        }

    });
    return LifecycleActivityView;

});