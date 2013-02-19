define([
	"require",
	"i18n!localization/nls/document-management-strings",
	"collections/folder",
	"common-objects/views/components/list_item",
	"views/folder_list",
	"views/folder_document_list",
	"views/folder_new",
	"views/folder_edit",
	"text!templates/folder_list_item.html"
], function (
	require,
	i18n,
	FolderList,
	ListItemView,
	FolderListView,
	FolderDocumentListView,
	FolderNewView,
	FolderEditView,
	template
) {
	var FolderListItemView = ListItemView.extend({
		template: Mustache.compile(template),
		tagName: "li",
		className: "folder",
		collection: function () {
			return new FolderList();
		},
		initialize: function () {
			ListItemView.prototype.initialize.apply(this, arguments);
			this.isOpen = false;
			if (this.model) {
				this.collection.parent = this.model;
			}
			this.events = _.extend(this.events, {
				"click .header .new-folder":	"actionNewFolder",
				"click .header .edit":			"actionEdit",
				"click .header .delete":		"actionDelete",
				"mouseleave .header":			"hideActions"
			});
			this.events['click [data-target="#items-' + this.cid + '"]'] = "toggle";
		},
		hideActions: function () {
			// Prevents the actions menu to stay opened all the time
			this.$el.find(".header .btn-group").first().removeClass("open");
		},
		modelToJSON: function () {
			data = this.model.toJSON();
			if (data.id) {
				data.path = data.id.replace(/^[^:]*:?/, "");
				this.modelPath = data.path;
			}
			return data;
		},
		rendered: function () {
			var isHome = this.model ? this.model.get("home") : false;
			var isRoot = _.isUndefined(this.model);
			if (isHome) this.$el.addClass("home");
			if (isRoot || isHome) {
				this.$(".delete").remove();
				this.$(".edit").remove();
			}

			var FolderListView = require("views/folder_list"); // Circular dependency
			this.foldersView = this.addSubView(
				new FolderListView({
					el: "#items-" + this.cid,
					collection: this.collection
				})
			).render();
			this.bind("shown", this.shown);
			this.bind("hidden", this.hidden);
		},
		show: function (routePath) {
			this.routePath = routePath;
			this.isOpen = true;
			this.foldersView.show();
			this.trigger("shown");
			this.collection.fetch({
				success: this.traverse
			});
		},
		shown: function () {
			this.$el.addClass("open");
			if (this.routePath) {
				// If from direct url acces (address bar)

				// show documents only if not traversed
				var pattern = new RegExp("^" + this.modelPath);
				if (this.routePath.match(pattern)) {
					this.showContent();
				}
			} else {
				// If not from direct url acces (click)
				this.showContent();
				this.navigate();
			}
		},
		showContent: function () {
			this.setActive();
			this.addSubView(new FolderDocumentListView({
				model: this.model
			})).render();
		},
		hide: function () {
			this.isOpen = false;
			this.foldersView.hide();
			this.trigger("hidden");
		},
		hidden: function () {
			this.$el.removeClass("open");
			this.navigate();
			this.showContent();
		},
		navigate: function () {
			var path = this.modelPath ? "/" + this.modelPath : "";
			this.router.navigate("folders" + path, {trigger: false});
		},
		setActive: function () {
			$("#document-menu .active").removeClass("active");
			this.$el.find(".header").first().addClass("active");
		},
		toggle: function () {
			this.isOpen ? this.hide() : this.show();
			return false;
		},
		traverse: function () {
			if (this.routePath) {
				var modelPath = this.modelPath;
				var routePath = this.routePath;
				_.each(this.foldersView.subViews, function (view) {
					var pattern = new RegExp("^" + view.modelPath);
					if (routePath.match(pattern)) {
						view.show(routePath);
					}
				});
			}
		},
		actionNewFolder: function (evt) {
			this.hideActions();
			var view = this.addSubView(
				new FolderNewView({
					collection: this.collection
				})
			);
			return false;
		},
		actionEdit: function () {
			this.hideActions();
			var view = this.addSubView(
				new FolderEditView({
					model: this.model
				})
			).show();
			return false;
		},
		actionDelete: function () {
			this.hideActions();
			if (confirm(i18n["DELETE_FOLDER_?"])) {
				this.model.destroy();
			}
			return false;
		}
	});
	return FolderListItemView;
});
