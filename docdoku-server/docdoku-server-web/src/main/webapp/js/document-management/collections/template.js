define([
	"models/template",
	"common/singleton_decorator"
], function (
	Template,
	singletonDecorator
) {
	var TemplateList = Backbone.Collection.extend({
		model: Template,
	});
	TemplateList.prototype.__defineGetter__("url", function () {
		return "/api/workspaces/" + APP_CONFIG.workspaceId + "/templates";
	});
	TemplateList = singletonDecorator(TemplateList);
    TemplateList.className="TemplateList";
	return TemplateList;
});
