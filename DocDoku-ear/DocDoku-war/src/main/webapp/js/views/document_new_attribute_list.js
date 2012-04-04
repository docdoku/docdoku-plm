DocumentNewAttributeListView = ListView.extend({
	ItemView: DocumentNewAttributeListItemView,
	collection: Backbone.Collection,
	template: "document-new-attribute-list-tpl",
	initialize: function () {
		ListView.prototype.initialize.apply(this, arguments);
		this.events["click .add"] = this.addAttribute;
	},
	collectionAdd: function (model) {
		this.createItemView(model);
	},
	addAttribute: function () {
		var model = this.collection.add({
			name: "",
			attributeType: "TEXT",
			value: ""
		});
	},
});
