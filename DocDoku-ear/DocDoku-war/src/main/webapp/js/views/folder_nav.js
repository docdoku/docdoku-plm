define([
	"common/singleton_decorator",
	"views/folder_list_item",
	"text!templates/folder_nav.html"
], function (
	singletonDecorator,
	FolderListItemView,
	template
) {
	var FolderNavView = FolderListItemView.extend({
		__name__: "FolderNavView",
		template: Mustache.compile(template),
		el: "#folder-nav",
		initialize: function () {
			FolderListItemView.prototype.initialize.apply(this, arguments);
			this.render();
		},
	});
	FolderNavView = singletonDecorator(FolderNavView);
	return FolderNavView;
});
