var TagDocumentList = Backbone.Collection.extend({
	model: Document,
});
TagDocumentList.prototype.__defineGetter__("url", function () {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/tags"
	return baseUrl + "/" + this.parent.get("label") + "/documents";
});
