define([
    "models/activity_model"
], function (ActivityModel) {

    var ActivityModels = Backbone.Collection.extend({

        model: ActivityModel
    });

    return ActivityModels;
});