define([
    "collections/task_models"
], function (TaskModels) {
    var ActivityModel = Backbone.Model.extend({

        defaults: function() {
            return {
                type: "SERIAL",
                taskModels: new TaskModels()
            };
        },

        initialize: function() {
            if(_.isUndefined(this.attributes.taskModels.models))
                this.attributes.taskModels = new TaskModels(this.attributes.taskModels);
        },

        toJSON: function() {
            var index = this.collection.indexOf(this);
            _.extend(this.attributes, {step: index});

            return _.clone(this.attributes);
        }

    });

    return ActivityModel;
});