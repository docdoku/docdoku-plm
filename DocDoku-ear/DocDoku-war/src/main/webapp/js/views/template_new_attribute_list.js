TemplateNewAttributeListView = ListView.extend({
	ItemView: TemplateNewAttributeListItemView,
	collectionAdd: function (model) {
		this.createItemView(model);
	},
});
