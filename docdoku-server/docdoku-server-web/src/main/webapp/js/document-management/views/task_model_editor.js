define([
    "models/task_model",
    "text!templates/task_model_editor.html"
], function (
    TaskModel,
    template
    ) {
    var TaskModelEditorView = Backbone.View.extend({

        tagName: "li",

        className: "task-section",

        events: {
            "click button.delete-task" : "deleteTaskAction"
        },

        initialize: function () {
            if(_.isUndefined(this.model))
                this.model = new TaskModel();

            this.template = Mustache.render(template, {task: this.model.attributes});
        },

        deleteTaskAction: function(){
            this.model.collection.remove(this.model);
            this.unbindAllEvents();
            this.remove();
        },

        render: function() {
            this.$el.html(this.template);
            return this;
        },

        unbindAllEvents: function(){
            this.undelegateEvents();
        }

    });
    return TaskModelEditorView;
});
