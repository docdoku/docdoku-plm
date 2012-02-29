var TemplateListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#template-list-item-tpl",
	initialize: function () {
		console.debug("TemplateListItemView.initialize");
		this.templateListItemViewBindings();
	},
	templateListItemViewBindings: function () {
		console.debug("TemplateListItemView.templateListItemViewBindings");
		this.baseViewBindings();
		_.bindAll(this, "isSelected", "delete");
	},
	onModelChange: function () {
		this.render();
	},
	modelToJSON: function () {
		console.debug("TemplateListItemView.modelToJSON");
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
