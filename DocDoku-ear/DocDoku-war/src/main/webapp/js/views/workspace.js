var WorkspaceView = BaseView.extend({
	template_el: "#workspace-tpl",
	renderAfter: function () {
		var rootFolderView = new FolderView({model:new RootFolder()});
		$("#folders-container").append(rootFolderView.el);
		var taglistView = new TagListView({
			el: $("#tags-container"),
			collection: new TagList()
		});
		taglistView.collection.fetch();
		return this;
	},
});
