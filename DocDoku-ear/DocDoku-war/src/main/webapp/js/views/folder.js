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
		view = new FolderView({model:folder});
		$(this.el).children(".subfolders").first().append(view.el);
		this.folderViews.push(view);
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
		if (this.documentsView) this.documentsView.remove();
		this.documentsView = new DocumentListView({model:this.model});
		$("#workspace .content").html(this.documentsView.el);

		$(this.el).hasClass("open") ? this.close() : this.open();
		return false;
	},
	mouseleave: function () {
		if ($(this.el).find(".actions").hasClass("open")) {
			$(this.el).find(".actions").click();
		}
		return false;
	},
	newFolder: function () {
		newView = new FolderNewView({model: this.model});
		newView.parent = this;
		this.mouseleave();
		newView.render();
		return false;
	},
	newDocument: function () {
		newView = new DocumentNewView({model: this.model});
		this.mouseleave();
		newView.render();
		return false;
	},
	edit: function () {
		editView = new FolderEditView({model: this.model});
		editView.parent = this;
		editView.render();
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
