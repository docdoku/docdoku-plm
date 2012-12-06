define([
    "models/task_model",
    "text!templates/task_model_editor.html"
], function (
    TaskModel,
    template
    ) {
    var TaskModelEditorView = Backbone.View.extend({

        template: Mustache.render(template),

        tagName: "li",
        className: "task-section",

        initialize: function () {
            this.model = new TaskModel();
        },

        render: function() {
            this.$el.html(this.template);
            return this;
        }

    });
    return TaskModelEditorView;
});
