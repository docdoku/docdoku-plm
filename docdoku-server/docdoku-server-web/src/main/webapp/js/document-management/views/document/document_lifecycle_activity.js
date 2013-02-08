define([
    "views/document/document_lifecycle_task",
    "i18n!localization/nls/document-management-strings",
    "text!templates/document/document_lifecycle_activity.html"

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

        render: function() {

            this.$el.html(Mustache.render(template, {i18n: i18n, activity:this.activity}));

            var completeClass = this.activity.complete ? "complete" : "incomplete";

            this.$el.addClass(this.activity.type.toLowerCase()).addClass(completeClass);

            var $tasks = this.$(".tasks");

            _.each(this.activity.tasks,function(task){
                $tasks.append(
                    new LifecycleTaskView().setTask(task).render().$el
                );
            });

            return this;

        }

    });
    return LifecycleActivityView;

});