define([
	"collections/workflow_models",
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
			return new WorkflowList;
		},
		itemViewFactory: function (model) {
			return new WorkflowListItemView({
				model: model
			});
		}
	});
	return WorkflowListView;
});
