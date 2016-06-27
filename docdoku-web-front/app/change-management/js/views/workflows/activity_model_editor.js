/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workflows/activity_model_editor.html',
    'common-objects/models/workflow/activity_model',
    'common-objects/models/task_model',
    'views/workflows/task_model_editor'
], function (Backbone, Mustache, template, ActivityModel, TaskModel, TaskModelEditorView) {
	'use strict';
    var ActivityModelEditorView = Backbone.View.extend({
        tagName: 'li',
        className: 'activity-section',

        events: {
            'click button.add-task': 'addTaskAction',
            'click button.switch-activity': 'switchActivityAction',
            'click button.delete-activity': 'deleteActivityAction',
            'change input.activity-state': 'lifeCycleStateChanged',
            'change input.tasksToComplete': 'tasksToCompleteChanged',
            'change select.relaunchActivitySelector': 'changeRelaunchActivityStep'
        },

        initialize: function () {
            this.subviews = [];

            var switchModeTitle;
            switch (this.model.get('type')) {
                case 'SEQUENTIAL':
                    switchModeTitle = App.config.i18n.GOTO_PARALLEL_MODE;
                    break;
                case 'PARALLEL':
                    switchModeTitle = App.config.i18n.GOTO_SEQUENTIAL_MODE;
                    break;
            }

            this.template = Mustache.render(template, {cid: this.model.cid, activity: this.model.attributes, switchModeTitle: switchModeTitle, i18n: App.config.i18n});

            this.model.attributes.taskModels.bind('add', this.addOneTask, this);
            this.model.attributes.taskModels.bind('remove', this.removeOneTask, this);

            this.on('activities-order:changed', this.populateRelaunchActivitySelector);
            this.on('activities:removed', this.populateRelaunchActivitySelector);

        },
        render: function () {
            this.$el.html(this.template);
            this.bindDomElements();
            this.addAllTask();
            this.populateRelaunchActivitySelector();
            return this;
        },
        bindDomElements: function () {
            var self = this;

            this.activityDiv = this.$('div.activity');

            this.buttonSwitchActivity = this.$('button.switch-activity');

            this.inputLifeCycleState = this.$('input.activity-state');

            this.inputTasksToComplete = this.$('input.tasksToComplete');

            this.relaunchActivitySelector = this.$('.relaunchActivitySelector');

            this.relaunchActivitySelectorWrapper = this.$('.relaunchActivitySelector-wrapper');

            this.tasksUL = this.$('ul.task-list');
            this.tasksUL.sortable({
                handle: 'i.fa.fa-bars',
                tolerance: 'pointer',
                start: function (event, ui) {
                    ui.item.oldPosition = ui.item.index();
                },
                stop: function (event, ui) {
                    self.taskPositionChanged(ui.item.oldPosition, ui.item.index());
                }
            });
        },

        addAllTask: function () {
            this.model.attributes.taskModels.each(this.addOneTask, this);
        },

        addOneTask: function (taskModel) {
            var _this = this;

            this.updateMaxTasksToComplete();

            var taskModelEditorView = new TaskModelEditorView({
                model: taskModel,
                roles: _this.options.roles,
                newRoles: _this.options.newRoles
            });

            _this.subviews.push(taskModelEditorView);
            taskModelEditorView.render();
            _this.tasksUL.append(taskModelEditorView.el);
        },

        removeOneTask: function () {
            this.updateMaxTasksToComplete();

            var cntTasks = this.model.get('taskModels').length;

            if (this.inputTasksToComplete.val() > cntTasks) {
                this.inputTasksToComplete.val(cntTasks);
                this.tasksToCompleteChanged();
            }
        },

        updateMaxTasksToComplete: function () {
            this.inputTasksToComplete.attr({
                MAX: this.model.get('taskModels').length
            });
        },

        addTaskAction: function () {
            this.inputTasksToComplete.val(parseInt(this.inputTasksToComplete.val(), 10) + 1);
            this.model.attributes.taskModels.add(new TaskModel());
            this.tasksToCompleteChanged();
            return false;
        },

        switchActivityAction: function () {
            switch (this.model.get('type')) {
                case 'SEQUENTIAL':
                    this.model.set({
                        type: 'PARALLEL'
                    });
                    this.activityDiv.removeClass('SEQUENTIAL');
                    this.activityDiv.addClass('PARALLEL');
                    this.buttonSwitchActivity.attr({title: App.config.i18n.GOTO_SEQUENTIAL_MODE});
                    break;
                case 'PARALLEL':
                    this.model.set({
                        type: 'SEQUENTIAL'
                    });
                    this.activityDiv.removeClass('PARALLEL');
                    this.activityDiv.addClass('SEQUENTIAL');
                    this.buttonSwitchActivity.attr({title: App.config.i18n.GOTO_PARALLEL_MODE});
                    break;
            }
            return false;
        },

        deleteActivityAction: function () {
            this.model.collection.remove(this.model);
            this.unbindAllEvents();
            this.remove();
            this.model.destroy();
        },

        tasksToCompleteChanged: function () {
            var tasksToCompleteValue = parseInt(this.inputTasksToComplete.val(), 10);
            var maxTaskToComplete = this.model.attributes.taskModels.length;
            if (tasksToCompleteValue <= 0) {
                this.inputTasksToComplete.val(1);
                this.model.set({
                    tasksToComplete: 1
                });
            } else if (tasksToCompleteValue > maxTaskToComplete) {
                this.inputTasksToComplete.val(maxTaskToComplete);
                this.model.set({
                    tasksToComplete: maxTaskToComplete
                });
            } else {
                this.model.set({
                    tasksToComplete: tasksToCompleteValue
                });
            }
        },

        taskPositionChanged: function (oldPosition, newPosition) {
            var taskModel = this.model.attributes.taskModels.at(oldPosition);
            this.model.attributes.taskModels.remove(taskModel, {silent: true});
            this.model.attributes.taskModels.add(taskModel, {silent: true, at: newPosition});
        },

        lifeCycleStateChanged: function () {
            this.model.set({
                lifeCycleState: this.inputLifeCycleState.val()
            });
        },

        populateRelaunchActivitySelector: function () {
            var that = this;
            this.relaunchActivitySelector.empty();
            this.relaunchActivitySelector.append('<option value="-1"></option>');
            var stepCount = 0;

            var modelIndex = this.model.collection.indexOf(this.model);

            this.model.collection.each(function (activity) {
                var activityIndex = activity.collection.indexOf(activity);
                if (activityIndex < modelIndex) {
                    if(activity.get('lifeCycleState')){
                        that.relaunchActivitySelector.append('<option value="' + activityIndex + '">' + activity.get('lifeCycleState') + '</option>');
                    }
                    stepCount++;
                }
            });

            if (!stepCount) {
                this.relaunchActivitySelectorWrapper.hide();
            } else {
                this.relaunchActivitySelectorWrapper.show();
            }

            if (this.model.get('relaunchStep') !== null) {
                this.relaunchActivitySelector.val(this.model.get('relaunchStep'));
            }

        },

        unbindAllEvents: function () {
            _.each(this.subviews, function (subview) {
                subview.unbindAllEvents();
            });
            this.undelegateEvents();
        },

        changeRelaunchActivityStep: function (e) {
            if (e.target.value === -1) {
                this.model.set('relaunchStep', null);
            } else {
                this.model.set('relaunchStep', e.target.value);
            }
        }

    });
    return ActivityModelEditorView;
});
