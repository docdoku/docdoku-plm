var DocumentListItemView = CheckboxListItemView.extend({
	tagName: "tr",
	template: "#document-list-item-tpl",
	modelToJSON: function () {
		var data = this.model.toJSON();

		// Format dates
		if (data.lastIteration && data.lastIteration.creationDate) {
			data.lastIteration.creationDate = app.formatDate(data.lastIteration.creationDate);
		}
		if (data.checkOutDate) {
			data.checkOutDate = app.formatDate(data.checkOutDate);
		}
		return data;
	},
});
