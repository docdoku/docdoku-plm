define([
    "common-objects/models/task_model"
], function (TaskModel) {

    var TaskModels = Backbone.Collection.extend({

        model: TaskModel
    });

    return TaskModels;
});
