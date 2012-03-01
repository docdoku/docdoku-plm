TagListItemView = BaseView.extend({
	tagName: "li",
	template_el: "#tag-list-item-tpl",
	events: {
		"click .name": "open",
		"click .actions .delete": "delete"
	},
	initialize: function () {
		this.baseViewBindings();
	},
	renderAfter: function () {
		$(this.el).addClass("tag");
	},
	open: function () {
		var view = new DocumentTagListView({
			el: $("#content"),
			collection: this.model.documents
		});
		view.collection.fetch();
		this.subViews.push(view);
	},
	delete: function () {
		if (confirm("Supprimer le libellé : " + this.model.get("label") + " ?")) {
			this.model.destroy();
		}
		return false;
	}
});
