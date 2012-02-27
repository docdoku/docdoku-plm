var TemplateList = Backbone.Collection.extend({});
TemplateList.prototype.__defineGetter__("url", function () {
	return "/api/workspaces/" + app.workspaceId + "/templates";
});
