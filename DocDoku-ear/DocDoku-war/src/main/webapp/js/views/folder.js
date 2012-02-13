var FolderView = Backbone.View.extend({
	tagName: "li",
	events: {
		"click .name": "toggle",
	},
	initialize: function () {
		_.bindAll(this,
			"template",
			"render",
			"onFolderListReset",
			"createFolderView",
			"open",
			"close",
			"toggle");
		this.folderViews = [];
		this.documentViews = [];
		this.render();
		this.model.folders.bind("reset", this.onFolderListReset);
	},
	onFolderListReset: function () {
		_.each(this.folderViews, function(view) {
			view.remove();
		});
		this.model.folders.each(this.createFolderView);
		$(this.el).children(".subfolders").first().collapse("show");
	},
	createFolderView: function (folder) {
		folder.folders = new FolderList();
		folder.folders.url = "/api/folders/" + folder.get("completePath");
		view = new FolderView({model:folder});
		$(this.el).children(".subfolders").first().append(view.el);
		this.folderViews.push(view);
	},
	createDocumentView: function (doc) {
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
		$(this.el).addClass("folder");
		if (this.model.get("isHome")) {
			$(this.el).addClass("home");
		}
		return this;
	},
	open: function () {
		$(this.el).addClass("open");
		this.model.folders.fetch();
	},
	close: function () {
		$(this.el).removeClass("open");
		$(this.el).children(".subfolders").first().collapse("hide");
	},
	toggle : function () {
		$(this.el).hasClass("open") ? this.close() : this.open();
		return false;
	}
});
