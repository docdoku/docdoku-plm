var DocumentCheckedoutList = Backbone.Collection.extend({
	model: Document,
});
DocumentCheckedoutList.prototype.__defineGetter__("url", function () {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/documents";
	return  baseUrl + "/checkedout";
});
