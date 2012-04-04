var WorkflowListView = CheckboxListView.extend({
	collection: WorkflowList,
	ItemView: WorkflowListItemView,
	template: "workflow-list-tpl",
});
