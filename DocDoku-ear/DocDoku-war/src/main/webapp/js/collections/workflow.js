var WorkflowList = Backbone.Collection.extend({
	model: Workflow,
});
WorkflowList.prototype.__defineGetter__("url", function () {
	return "/api/workspaces/" + app.workspaceId + "/workflows";
});
