var DocumentListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#document-list-item-tpl",
	events: {
		"click input.select": "selectToggle",
	},
	initialize: function () {
		this.baseViewBindings();
		_.bindAll(this,
			"template", "render",
			"selectToggle",
			"checkout", "undocheckout", "checkin",
			"delete");
			this.model.bind("change", this.render);
		this.isSelected = false;
	},
	modelToJSON: function () {
		var data = this.model.toJSON();
		// Format dates
		if (data.lastIteration && data.lastIteration.creationDate) {
			data.lastIteration.creationDate = new Date(data.lastIteration.creationDate).format("dd/mm/yyyy");
		}
		if (data.checkOutDate) {
			data.checkOutDate = new Date(data.checkOutDate).format("dd/mm/yyyy");
		}
		return data;
	},
	onModelSync: function () {
		this.render();
	},
	renderAfter: function () {
		if (this.isSelected) {
			$(this.el).find("input.select").first().attr("checked", true);
		}
	},
	selectToggle: function () {
		this.isSelected = $(this.el).find("input.select").first().is(":checked");
	},
	checkout: function () {
		if (this.isSelected) {
			this.model.checkout();
		}
	},
	undocheckout: function () {
		if (this.isSelected) {
			this.model.undocheckout();
		}
	},
	checkin: function () {
		if (this.isSelected) {
			this.model.checkin();
		}
	},
	delete: function () {
		if (this.isSelected) {
			this.model.destroy();
		}
	}
});
