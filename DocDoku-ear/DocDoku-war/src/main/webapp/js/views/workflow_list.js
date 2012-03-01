var WorkflowListView = ListView.extend({
	ItemView: WorkflowListItemView,
	tagName: "div",
	template_el: "#workflow-list-tpl",
	events: {
		"click tbody tr input": "itemSelectClicked",
		"click .actions .new": "new",
		"click .actions .delete": "delete"
	},
	initialize: function () {
		this.workflowListViewBindings();
		this.collection.fetch();
	},
	workflowListViewBindings: function () {
		this.listViewBindings();
		_.bindAll(this, "itemSelectClicked", "new", "delete");
	},
	itemSelectClicked: function () {
		if ($(this.el).find("input.select[type=checkbox]").filter(":checked").length > 0) {
			$(this.el).find(".actions .delete").show();
		} else {
			$(this.el).find(".actions .delete").hide();
		}
	},
	new : function () {
		var view = new WorkflowNewView({model: this.model});
		this.subViews.push(view);
		view.render();
		return false;
	},
	delete: function () {
		_.each(this.itemViews, function (view) {
			view.delete();
		});
		return false;
	}
});
