var FolderListView = CollapsibleListView.extend({
	itemViewFactory: function (model) { return new FolderListItemView({ model: model }); },
});
