TemplateList = Backbone.Collection.extend({
	initialize: function () {
		this.url = "/api/workspaces/" + app.workspaceId + "/templates";
	}
});
