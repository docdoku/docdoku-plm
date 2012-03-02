var FolderView = BaseView.extend({
	tagName: "li",
	template_el: "#folder-tpl",
	events: {
		"click .name, .icon.status, .icon.type": "toggle",
		"click .actions .new-folder": "newFolder",
		"click .actions .new-document": "newDocument",
		"click .actions .edit": "edit",
		"click .actions .delete": "delete",
		"mouseleave .header": "mouseleave",
	},
	initialize: function () {
		this.baseViewBindings();
		_.bindAll(this,
			"template", "render",
			"onFolderListReset", "createFolderView",
			"open", "opened", "close", "toggle",
			"mouseleave",
			"newFolder","newDocument",
			"edit", "delete");
		this.folderViews = [];
		this.isOpen = false;
		this.isHome = this.model.get("home");
		this.render();
		this.model.folders.bind("reset", this.onFolderListReset);
	},
	onFolderListReset: function () {
		_.each(this.folderViews, function(view) {
			view.remove();
		});
		this.model.folders.each(this.createFolderView);
		this.opened();
	},
	createFolderView: function (folder) {
		folder.parent = this.model;
		var view = new FolderView({model:folder});
		this.subViews.push(view);
		this.folderViews.push(view);
		$(this.el).children(".subfolders").first().append(view.el);
	},
	render: function () {
		$(this.el).html(this.template({
			model: this.model.toJSON()
		}));
		$(this.el).addClass("folder");
		if (this.isHome) $(this.el).addClass("home");
		// No delete or no edit actions on root or home
		if (this.model.parent == undefined || this.isHome) {
			$(this.el).find(".actions .delete").remove();
			$(this.el).find(".actions .edit").remove();
		}
		// If actions menu is empty
		if ($(this.el).find('.dropdown-menu').children().length < 1) {
			$(this.el).find(".actions").remove();
		}
		return this;
	},
	open: function () {
		this.mouseleave();
		this.model.folders.fetch();
	},
	// Launched at onFolderListReset end
	opened: function () {
		$(this.el).addClass("open");
		$(this.el).children(".subfolders").first().collapse("show");
		this.isOpen = true;
	},
	close: function () {
		this.mouseleave();
		$(this.el).children(".subfolders").collapse("hide");
		$(this.el).removeClass("open");
		this.isOpen = false;
	},
	toggle : function () {
		if (app.contentView) $(app.contentView.el).html("");
		delete app.contentView;
		var view = new DocumentListView({collection:this.model.documents});
		app.contentView = view;
		$("#content").html(view.el);
		folderId = this.model.id ? this.model.id : "";

		$(this.el).hasClass("open") ? this.close() : this.open();
		var route = this.model.id ? "folders/" + this.model.id : "folders";
		app.router.navigate(route);
		return false;
	},
	mouseleave: function () {
		if ($(this.el).find(".actions").hasClass("open")) {
			$(this.el).find(".actions").click();
		}
		return false;
	},
	newFolder: function () {
		var view = new FolderNewView({collection: this.model.folders});
		this.subViews.push(view);
		view.parent = this;
		this.mouseleave();
		view.render();
		return false;
	},
	newDocument: function () {
		var view = new DocumentNewView({collection: this.model.documents});
		this.subViews.push(view);
		this.mouseleave();
		view.render();
		return false;
	},
	edit: function () {
		var view = new FolderEditView({model: this.model});
		this.subViews.push(view);
		view.parent = this;
		view.render();
		return false;
	},
	delete: function () {
		if (confirm("Supprimer le dossier : " + this.model.get("name") + " ?")) {
			this.model.destroy();
			this.remove();
		}
		return false;
	}
});
