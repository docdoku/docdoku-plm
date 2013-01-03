define([
    "collections/activity_models"
], function (ActivityModels) {
	var WorkflowModel = Backbone.Model.extend({

        urlRoot: "/api/workspaces/" + APP_CONFIG.workspaceId + "/workflows",

        defaults: function() {
            return {
                activityModels: new ActivityModels()
            };
        },

        parse: function(response) {
            response.activityModels = new ActivityModels(response.activityModels);
            return response;
        }

    });
	return WorkflowModel;
});
