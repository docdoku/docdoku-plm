define([
	"require",
	"views/collapsible_list",
	"views/folder_list_item"
], function (
	require,
	CollapsibleListView,
	FolderListItemView
) {
	var FolderListView = CollapsibleListView.extend({
		__name__: "FolderListView",
		itemViewFactory: function (model) {
			var FolderListItemView = require("views/folder_list_item"); // Circular dependency
			return new FolderListItemView({
				model: model
			});
		},
	});
	return FolderListView;
});
