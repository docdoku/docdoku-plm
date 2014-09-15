/*global define*/
define([
    'backbone',
    "common-objects/collections/activity_models"
], function (Backbone, ActivityModels) {
    var WorkflowModel = Backbone.Model.extend({

        urlRoot: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/workflows";
        },

        defaults: function () {
            return {
                activityModels: new ActivityModels()
            };
        },

        parse: function (response) {
            response.activityModels = new ActivityModels(response.activityModels);
            return response;
        }

    });
    return WorkflowModel;
});
