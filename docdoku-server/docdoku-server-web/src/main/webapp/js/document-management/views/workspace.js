define([
	"common-objects/views/base",
	"text!templates/workspace.html"
], function (
	BaseView,
	template
) {
	var WorkspaceView = BaseView.extend({
		template: Mustache.compile(template),
	});
	return WorkspaceView;
});
