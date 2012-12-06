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

        template: Mustache.render(template),

        tagName: "li",
        className: "activity-section",

        events: {
            "click button.add-task" : "addTaskAction"
        },

        initialize: function () {
            this.model = new ActivityModel();
            this.model.attributes.taskModels.bind('add', this.addOneTask, this);
            this.model.attributes.taskModels.bind('reset', this.addAllTask, this);
        },

        addAllTask: function() {
            this.collection.each(this.addOne, this);
        },

        addOneTask: function(taskModel) {
            var taskModelEditorView = new TaskModelEditorView({model: taskModel});
            taskModelEditorView.render();
            this.tasksUL.append(taskModelEditorView.el);
        },

        addTaskAction: function(){
            this.model.attributes.taskModels.add(new TaskModel());
            return false;
        },

        render: function() {
            this.$el.html(this.template);
            this.tasksUL = this.$el.find("ul.task-list");
            return this;
        }

    });
    return ActivityModelEditorView;
});