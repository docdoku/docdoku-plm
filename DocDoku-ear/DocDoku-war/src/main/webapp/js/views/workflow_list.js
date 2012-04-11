var WorkflowListView = CheckboxListView.extend({
	collection: function () { return WorkflowList.getInstance(); },
	ItemView: WorkflowListItemView,
	template: "workflow-list-tpl",
});
