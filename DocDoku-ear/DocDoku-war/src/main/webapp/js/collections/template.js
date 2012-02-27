TemplateList = Backbone.Collection.extend({
	url: function () {
		return "/api/workspaces/" + app.workspaceId + "/templates";
	}
});
