TemplateNewAttributeListView = ListView.extend({
	itemViewFactory: function (model) { return new TemplateNewAttributeListItemView({ model: model }); },
	collectionAdd: function (model) {
		this.createItemView(model);
	},
});
