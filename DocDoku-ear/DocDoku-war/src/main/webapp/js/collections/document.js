var DocumentList = Backbone.Collection.extend({
	model: Document,
	url: function () {
		baseUrl = "/api/workspaces/" + app.workspaceId + "/folders";
		return  baseUrl + "/" + this.parent.id + "/documents";
	}
});
