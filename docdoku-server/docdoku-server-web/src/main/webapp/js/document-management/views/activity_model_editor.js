define([
    "i18n",
    "models/activity_model",
    "text!templates/activity_model_editor.html",
    "models/task_model",
    "views/task_model_editor"
], function (
    i18n,
    ActivityModel,
    template,
    TaskModel,
    TaskModelEditorView
    ) {
    var ActivityModelEditorView = Backbone.View.extend({

        tagName: "li",
        className: "activity-section",

        events: {
            "click button.add-task" : "addTaskAction",
            "click button.switch-activity" : "switchActivityAction",
            "click button.delete-activity" : "deleteActivityAction",
            "change input.activity-name":  "lifeCycleStateChanged",
            "change input.tasksToComplete":  "tasksToCompleteChanged"
        },

        initialize: function () {

            this.subviews = [];

            this.template = Mustache.render(template,{cid: this.model.cid, activity: this.model.attributes, i18n: i18n});

            this.model.attributes.taskModels.bind('add', this.addOneTask, this);
            this.model.attributes.taskModels.bind('remove', this.removeOneTask, this);

        },

        addAllTask: function() {
            this.model.attributes.taskModels.each(this.addOneTask, this);
        },

        addOneTask: function(taskModel) {

            this.updateMaxTasksToComplete();

            var taskModelEditorView = new TaskModelEditorView({model: taskModel, users: this.options.users});
            this.subviews.push(taskModelEditorView);
            taskModelEditorView.render();
            this.tasksUL.append(taskModelEditorView.el);
        },

        removeOneTask: function(){
            this.updateMaxTasksToComplete();
        },

        updateMaxTasksToComplete: function(){
            this.inputTasksToComplete.attr({
                MAX: this.model.get("taskModels").length
            });
        },

        addTaskAction: function(){
            this.model.attributes.taskModels.add(new TaskModel());
            return false;
        },

        switchActivityAction: function(){
            switch(this.model.get("type")){
                case "SERIAL":
                    this.model.set({
                        type: "PARALLEL"
                    });
                    this.activityDiv.removeClass("SERIAL");
                    this.activityDiv.addClass("PARALLEL");
                    break;
                case "PARALLEL":
                    this.model.set({
                        type: "SERIAL"
                    });
                    this.activityDiv.removeClass("PARALLEL");
                    this.activityDiv.addClass("SERIAL");
                    break;
            }
            return false;
        },

        deleteActivityAction: function(){
            this.model.collection.remove(this.model);
            this.unbindAllEvents();
            this.remove();
        },

        tasksToCompleteChanged: function(){
            this.model.set({
                tasksToComplete: this.inputTasksToComplete.val()
            });
        },

        taskPositionChanged: function(oldPosition, newPosition){
            var taskModel = this.model.attributes.taskModels.at(oldPosition);
            this.model.attributes.taskModels.remove(taskModel, {silent:true});
            this.model.attributes.taskModels.add(taskModel, {silent:true, at:newPosition});
        },

        lifeCycleStateChanged: function(){
            this.model.set({
                lifeCycleState: this.inputLifeCycleState.val()
            });
        },

        render: function() {
            this.$el.html(this.template);

            this.bindDomElements();

            this.addAllTask();

            return this;
        },

        bindDomElements: function(){
            var self = this;

            this.activityDiv = this.$("div.activity");

            this.inputLifeCycleState = this.$('input.activity-name');

            this.inputTasksToComplete = this.$('input.tasksToComplete');

            this.tasksUL = this.$("ul.task-list");

            this.tasksUL.sortable({
                start: function(event, ui) {
                    ui.item.oldPosition = ui.item.index();
                },
                stop: function(event, ui) {
                    self.taskPositionChanged(ui.item.oldPosition, ui.item.index());
                }
            });
        },

        unbindAllEvents: function(){
            _.each(this.subviews, function(subview){
                subview.unbindAllEvents();
            });
            this.undelegateEvents();
        }

    });
    return ActivityModelEditorView;
});