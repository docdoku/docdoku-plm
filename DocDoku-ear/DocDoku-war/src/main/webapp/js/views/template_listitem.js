var TemplateListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#template-list-item-tpl",
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"isSelected",
			"delete");
			this.model.bind("change", this.render);
	},
	formatData: function (data) {
		// Format dates
		data.creationDate = new Date(data.creationDate).toLocaleDateString();
		return data;
	},
	render: function () {
		$(this.el).html(this.template({
			model: this.formatData(this.model.toJSON())
		}));
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
