define([
	"common-objects/models/workflow_model"
], function (
	WorkflowModel
) {
	var WorkflowModels = Backbone.Collection.extend({
		model: WorkflowModel,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/workflows"
	});

	return WorkflowModels;
});
