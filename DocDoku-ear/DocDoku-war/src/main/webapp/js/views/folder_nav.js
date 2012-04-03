var FolderNavView = FolderListItemView.extend({
	template: "#folder-nav-tpl",
	el: "#folder-nav",
	initialize: function () {
		FolderListItemView.prototype.initialize.apply(this, arguments);
		this.render();
	},
});
FolderNavView = singletonDecorator(FolderNavView);
