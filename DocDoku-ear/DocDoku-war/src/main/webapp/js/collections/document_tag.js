var DocumentTagList = Backbone.Collection.extend({
});
DocumentTagList.prototype.__defineGetter__("url", function () {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/tags"
	return baseUrl + "/" + this.parent.get("id") + "/documents";
});
