var DocumentIterationList = Backbone.Collection.extend({
});
DocumentIterationList.prototype.__defineGetter__("url", function () {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/documents"
	return baseUrl + "/" + this.parent.get("id") + "/iterations";
});
