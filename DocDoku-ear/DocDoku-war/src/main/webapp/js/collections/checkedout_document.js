var CheckedoutDocumentList = Backbone.Collection.extend({
	model: Document,
});
CheckedoutDocumentList.prototype.__defineGetter__("url", function () {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/documents";
	return  baseUrl + "/checkedout";
});
