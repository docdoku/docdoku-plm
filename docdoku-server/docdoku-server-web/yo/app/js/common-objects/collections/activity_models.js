define([
    'backbone',
    "common-objects/models/activity_model"
], function (Backbone, ActivityModel) {

    var ActivityModels = Backbone.Collection.extend({

        model: ActivityModel

    });

    return ActivityModels;
});