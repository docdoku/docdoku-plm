define([
	"collections/workflow",
	"views/checkbox_list",
	"views/workflow_list_item",
	"text!templates/workflow_list.html"
], function (
	WorkflowList,
	CheckboxListView,
	WorkflowListItemView,
	template
) {
	var WorkflowListView = CheckboxListView.extend({
		template: Mustache.compile(template),
		collection: function () {
			return WorkflowList.getInstance();
		},
		itemViewFactory: function (model) {
			return new WorkflowListItemView({
				model: model
			});
		},
	});
	return WorkflowListView;
});
