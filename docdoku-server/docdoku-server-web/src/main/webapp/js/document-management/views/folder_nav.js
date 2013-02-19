define([
	"common-objects/common/singleton_decorator",
	"views/folder_list_item",
	"text!templates/folder_nav.html"
], function (
	singletonDecorator,
	FolderListItemView,
	template
) {
	var FolderNavView = FolderListItemView.extend({
		template: Mustache.compile(template),
		el: "#folder-nav",
		initialize: function () {
			FolderListItemView.prototype.initialize.apply(this, arguments);
			this.render();
		}
	});
	FolderNavView = singletonDecorator(FolderNavView);
	return FolderNavView;
});
