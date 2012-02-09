var WorkspaceView = Backbone.View.extend({
	initialize: function () {
		this.foldersView = new FoldersView({
			collection: this.model.folderList
		});
		_.bindAll(this, "render");
	},
	render: function () {
		this.foldersView.render();
		return this;
	}
});
