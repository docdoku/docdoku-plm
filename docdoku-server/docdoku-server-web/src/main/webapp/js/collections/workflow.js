define([
	"models/workflow",
	"common/singleton_decorator"
], function (
	Workflow,
	singletonDecorator
) {
	var WorkflowList = Backbone.Collection.extend({
		model: Workflow
	});
	WorkflowList.prototype.__defineGetter__("url", function () {
		return "/api/workspaces/" + APP_CONFIG.workspaceId + "/workflows";
	});
	WorkflowList = singletonDecorator(WorkflowList);
    WorkflowList.className="WorkflowList";
	return WorkflowList;
});
