var TemplateList = Backbone.Collection.extend({
	model: Template,
});
TemplateList.prototype.__defineGetter__("url", function () {
	return "/api/workspaces/" + app.workspaceId + "/templates";
});
TemplateList = singletonDecorator(TemplateList);
