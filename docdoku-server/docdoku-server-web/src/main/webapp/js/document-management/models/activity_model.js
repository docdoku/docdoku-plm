define([
    "collections/task_models"
], function (TaskModels) {
    var ActivityModel = Backbone.Model.extend({

        defaults: function() {
            return {
                taskModels: new TaskModels()
            };
        },

        parse: function(response) {
            response.taskModels = new TaskModels(response.taskModels);
            return response;
        }
    });

    return ActivityModel;
});