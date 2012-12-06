define([
	"models/workflow"
], function (
	Workflow
) {
	var WorkflowList = Backbone.Collection.extend({
		model: Workflow,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/workflows"
	});

	return WorkflowList;
});
