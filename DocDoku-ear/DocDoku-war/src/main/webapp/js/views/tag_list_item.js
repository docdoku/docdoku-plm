TagListItemView = ListItemView.extend({
	tagName: "li",
	className: "tag",
	template: "tag-list-item-tpl",
	initialize: function () {
		ListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"click .actions .edit": "actionEdit",
			"click .actions .delete": "actionDelete",
			"mouseleave .header": "hideActions"
		});
	},
	hideActions: function () {
		// Prevents the actions menu to stay opened all the time
		this.$el.find(".header .btn-group").first().removeClass("open");
	},
	setActive: function () {
		$("#nav .active").removeClass("active");
		this.$el.find(".nav-list-entry").first().addClass("active");
	},
	showContent: function () {
		this.setActive();
		this.addSubView(new TagDocumentListView({
			model: this.model
		})).render();
	},
	actionEdit: function () {
		this.hideActions();
		var view = this.addSubView(new TagEditView({model: this.model}));
		view.render();
		return false;
	},
	actionDelete: function () {
		this.hideActions();
		if (confirm("Supprimer le libellé : " + this.model.get("label") + " ?")) {
			this.model.destroy();
		}
		return false;
	},
});
