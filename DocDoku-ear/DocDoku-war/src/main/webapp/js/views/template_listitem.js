var TemplateListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#template-list-item-tpl",
	initialize: function () {
		this.templateListItemViewBindings();
	},
	templateListItemViewBindings: function () {
		this.baseViewBindings();
		_.bindAll(this, "isSelected", "delete");
	},
	onModelChange: function () {
		this.render();
	},
	modelToJSON: function () {
		var data = this.model.toJSON();
		// Format dates
		data.creationDate = new Date(data.creationDate).format("dd/mm/yyyy");
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
