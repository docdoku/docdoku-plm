var DocumentList = Backbone.Collection.extend({
	model: Document,
});
DocumentList.prototype.__defineGetter__("url", function () {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/folders";
	return  baseUrl + "/" + this.parent.id + "/documents";
});
