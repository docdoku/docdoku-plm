var FolderView = Backbone.View.extend({
	tagName: "li",
	events: {
		"click .icon": "toggle"
	},
	initialize: function () {
		_.bindAll(this,
			"template",
			"render",
			"onCollectionReset",
			"createFolderView",
			"toggle");
		this.views = [];
		this.render();
		this.collection.bind("reset", this.onCollectionReset);
		// Fetch should not happen here but is buggy
		//this.collection.fetch();
	},
	onCollectionReset: function () {
		_.each(this.views, function(view) {
			view.remove();
		});
		this.collection.each(this.createFolderView);
		$(this.el).children(".subfolders").first().collapse("show");
	},
	createFolderView: function (folder) {
		folders = new FolderList();
		folders.url = "/api/folders/" + folder.get("completePath");
		view = new FolderView({
			model: folder,
			collection: folders
		});
		$(this.el).children(".subfolders").first().append(view.el);
		this.views.push(view);
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
		return this;
	},
	toggle : function () {
		if ($(this.el).hasClass("open")) {
			console.log(this.cid, "close");
			$(this.el).removeClass("open");
			$(this.el).children(".subfolders").first().collapse("hide");
		} else {
			console.log(this.cid, "open");
			$(this.el).addClass("open");
			this.collection.fetch();
		}
		return false;
	}
});
