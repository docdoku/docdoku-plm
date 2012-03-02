var TemplateListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#template-list-item-tpl",
	events: {
		"click input.select": "onSelectToggle",
	},
	initialize: function () {
		this.templateListItemViewBindings();
	},
	templateListItemViewBindings: function () {
		this.baseViewBindings();
		_.bindAll(this,
			"select", "onSelectToggle",
			"delete");
		this.wasSelected = false;
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
	renderAfter: function () {
		$(this.el).find("input.select").first().attr("checked", this.wasSelected);
	},
	select: function (value) {
		this.wasSelected = value;
		$(this.el).find("input.select").first().attr("checked", value);
	},
	onSelectToggle: function () {
		this.wasSelected = $(this.el).find("input.select").first().is(":checked");
	},
	isSelected: function () {
		return $(this.el).find("input.select").first().is(":checked");
	},
	delete: function () {
		if (this.isSelected()) {
			this.model.destroy();
		}
	}
});
