DocumentNewAttributeListView = ListView.extend({
	typeViewMapping: {
			"BOOLEAN":	DocumentNewAttributeListItemBooleanView,
			"DATE":		DocumentNewAttributeListItemDateView,
			"NUMBER":	DocumentNewAttributeListItemNumberView,
			"TEXT":		DocumentNewAttributeListItemTextView,
			"URL":		DocumentNewAttributeListItemUrlView,
	},
	itemViewFactory: function (model) {
		var type = model.get("type");
		return new this.typeViewMapping[type]({ model: model });
	},
	collectionAdd: function (model) {
		this.createItemView(model);
	},
});
