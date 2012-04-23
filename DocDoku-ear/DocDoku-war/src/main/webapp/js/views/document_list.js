var DocumentListView = CheckboxListView.extend({
	itemViewFactory: function (model) { return new DocumentListItemView({ model: model }); },
	template: "document-list-tpl",
});
