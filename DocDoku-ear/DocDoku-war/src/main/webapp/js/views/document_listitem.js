var DocumentListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#document-list-item-tpl",
	events: {
		"click input.select": "onSelectToggle",
	},
	initialize: function () {
		this.baseViewBindings();
		_.bindAll(this,
			"template", "render",
			"select", "onSelectToggle", "isSelected",
			"checkout", "undocheckout", "checkin",
			"delete");
			this.model.bind("change", this.render);
		this.wasSelected = false;
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
	checkout: function () {
		if (this.isSelected()) {
			this.model.checkout();
		}
	},
	undocheckout: function () {
		if (this.isSelected()) {
			this.model.undocheckout();
		}
	},
	checkin: function () {
		if (this.isSelected()) {
			this.model.checkin();
		}
	},
	delete: function () {
		if (this.isSelected()) {
			this.model.destroy();
		}
	}
});
