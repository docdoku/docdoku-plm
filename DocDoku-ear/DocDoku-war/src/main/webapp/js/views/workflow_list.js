var WorkflowListView = CheckboxListView.extend({
	collection: function () { return WorkflowList.getInstance(); },
	itemViewFactory: function (model) { return new WorkflowListItemView({ model: model }); },
	template: "workflow-list-tpl",
});
