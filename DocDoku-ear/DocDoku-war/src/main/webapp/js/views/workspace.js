var WorkspaceView = BaseView.extend({
	template_el: "#workspace-tpl",
	initialize: function () {
		this.baseViewBindings();
	},
	renderAfter: function () {
		var rootFolderView = new FolderView({model:new RootFolder()});
		this.subViews.push(rootFolderView);
		$("#folders-container").append(rootFolderView.el);
		var taglistView = new TagListView({
			el: $("#tags-container"),
			collection: new TagList()
		});
		this.subViews.push(taglistView);
		taglistView.collection.fetch();
		return this;
	},
});
