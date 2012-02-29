var DocumentCheckedoutListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#document-list-item-tpl",
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"isSelected",
			"undocheckout", "checkin",
			"delete");
			this.model.bind("change", this.render);
	},
	formatData: function (data) {
		// Format dates
		if (data.lastIteration && data.lastIteration.creationDate) {
			data.lastIteration.creationDate = new Date(data.lastIteration.creationDate).toLocaleDateString();
		}
		if (data.checkOutDate) {
			data.checkOutDate = new Date(data.checkOutDate).toLocaleDateString();
		}
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
