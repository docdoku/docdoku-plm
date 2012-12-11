define([
    "models/activity_model",
    "text!templates/activity_model_editor.html",
    "models/task_model",
    "views/task_model_editor"
], function (
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
            "click button.delete-activity" : "deleteActivityAction"
        },

        initialize: function () {

            this.subviews = [];

            this.template = Mustache.render(template,{activity: this.model.attributes});

            this.model.attributes.taskModels.bind('add', this.addOneTask, this);

        },

        addAllTask: function() {
            this.model.attributes.taskModels.each(this.addOneTask, this);
        },

        addOneTask: function(taskModel) {
            var taskModelEditorView = new TaskModelEditorView({model: taskModel});
            this.subviews.push(taskModelEditorView);
            taskModelEditorView.render();
            this.tasksUL.append(taskModelEditorView.el);
        },

        addTaskAction: function(){
            this.model.attributes.taskModels.add(new TaskModel({
                title: "Task "+this.model.attributes.taskModels.length
            }));
            return false;
        },

        deleteActivityAction: function(){
            this.model.collection.remove(this.model);
            this.unbindAllEvents();
            this.remove();
        },

        taskPositionChanged: function(oldPosition, newPosition){
            var taskModel = this.model.attributes.taskModels.at(oldPosition);
            this.model.attributes.taskModels.remove(taskModel, {silent:true});
            this.model.attributes.taskModels.add(taskModel, {silent:true, at:newPosition});
        },

        render: function() {
            var self = this;

            this.$el.html(this.template);

            this.tasksUL = this.$el.find("ul.task-list");

            this.tasksUL.sortable({
                start: function(event, ui) {
                    ui.item.oldPosition = ui.item.index();
                },
                stop: function(event, ui) {
                    self.taskPositionChanged(ui.item.oldPosition, ui.item.index());
                }
            });

            this.addAllTask();

            return this;
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