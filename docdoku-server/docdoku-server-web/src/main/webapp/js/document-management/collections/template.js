define([
	"models/template",
	"common-objects/common/singleton_decorator"
], function (
	Template,
	singletonDecorator
) {
	var TemplateList = Backbone.Collection.extend({
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/templates",
		model: Template
	});

	TemplateList = singletonDecorator(TemplateList);
    TemplateList.className="TemplateList";

	return TemplateList;
});
