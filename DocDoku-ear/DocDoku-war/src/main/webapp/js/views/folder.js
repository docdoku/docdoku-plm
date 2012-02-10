var FolderView = Backbone.View.extend({
	tagName: 'li',
	initialize: function () {
		_.bindAll(this,
			"template",
			"render",
			"onCollectionReset",
			"createFolderView",
			"open");
		this.render();
		this.views = [];
		this.collection.bind("reset", this.onCollectionReset);
		this.collection.fetch();
		$(this.el).children(".subfolders").on("show", this.open);
	},
	onCollectionReset: function () {
		_.each(this.views, function(view) {
			view.remove();
		});
		this.collection.each(this.createFolderView);
	},
	createFolderView: function (folder) {
		folders = new FolderList();
		folders.url = "/api/folders/" + folder.get("completePath");
		view = new FolderView({
			model: folder,
			collection: folders
		});
		$(this.el).children(".subfolders").first().append(view.el);
		this.views[folder.id] = view;
	},
	template: function(data) {
		data.view_cid = this.cid;
		return Mustache.render(
			$("#folder-tpl").html(),
			data
		);
	},
	render: function () {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	},
	open : function () {
		//this.collection.fetch();
	}
});
