/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'common-objects/views/workflow/lifecycle_task',
    'text!common-objects/templates/workflow/lifecycle_activity.html'

], function (Backbone, Mustache, LifecycleTaskView, template) {

    'use strict';

    var LifecycleActivityView = Backbone.View.extend({

        tagName: 'div',
        className: 'activity well',

        events: {
        },

        initialize: function () {
        },

        setActivity: function (activity) {
            this.activity = activity;
            return this;
        },

        setEntityType: function (entityType) {
            this.entityType = entityType;
            return this;
        },

        render: function () {

            var that = this;

            switch (this.activity.type) {
                case 'SERIAL':
                    this.activityType = App.config.i18n.SERIAL_ACTIVITY;
                    break;
                case 'PARALLEL':
                    this.activityType = App.config.i18n.PARALLEL_ACTIVITY + ' ' + this.activity.tasksToComplete;
                    break;
            }

            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, activity: this.activity, activityType: this.activityType}));

            var completeClass = 'incomplete';

            if (this.activity.complete) {
                completeClass = 'complete';
            }
            if (this.activity.stopped) {
                completeClass = 'rejected';
            }
            if (this.activity.inProgress) {
                completeClass = 'in_progress';
            }

            this.$el.addClass(this.activity.type.toLowerCase()).addClass(completeClass);

            var $tasks = this.$('.tasks');

            _.each(this.activity.tasks, function (task, index) {

                task.parentWorkflowId = that.activity.parentWorkflowId;
                task.parentActivityStep = that.activity.step;
                task.index = index;

                if ((that.activity.stopped || that.activity.complete) && task.status.toLowerCase() === 'in_progress') {
                    task.status = 'NOT_STARTED';                                                                          // Disable task if activity is close
                }

                var lifecycleTaskView = new LifecycleTaskView().setTask(task).setEntityType(that.entityType).render();

                $tasks.append(lifecycleTaskView.$el);
                lifecycleTaskView.on('task:change', function () {
                    that.trigger('activity:change');
                });

            });

            return this;

        }

    });
    return LifecycleActivityView;

});
