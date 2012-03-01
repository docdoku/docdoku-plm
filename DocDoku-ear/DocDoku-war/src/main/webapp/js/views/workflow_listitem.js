var WorkflowListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#workflow-list-item-tpl",
	initialize: function () {
		this.workflowListItemViewBindings();
	},
	workflowListItemViewBindings: function () {
		this.baseViewBindings();
		_.bindAll(this, "isSelected", "delete");
	},
	onModelChange: function () {
		this.render();
	},
	modelToJSON: function () {
		var data = this.model.toJSON();
		// Format dates
		data.creationDate = new Date(data.creationDate).toLocaleDateString();
		return data;
	},
	isSelected: function () {
		return $(this.el).find("input.select").filter(":checked").length > 0;
	},
	delete: function () {
		if (this.isSelected()) {
			this.model.destroy();
		}
	}
});
