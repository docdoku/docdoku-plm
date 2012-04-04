var TemplateListItemView = CheckboxListItemView.extend({
	tagName: "tr",
	template: "template-list-item-tpl",
	modelToJSON: function () {
		var data = this.model.toJSON();
		// Format dates
		data.creationDate = app.formatDate(data.creationDate);
		return data;
	},
});
