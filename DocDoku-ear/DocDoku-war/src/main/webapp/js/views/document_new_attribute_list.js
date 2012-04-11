DocumentNewAttributeListView = ListView.extend({
	ItemView: DocumentNewAttributeListItemView,
	collectionAdd: function (model) {
		this.createItemView(model);
	},
});
