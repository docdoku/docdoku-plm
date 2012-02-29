TagListItemView = BaseView.extend({
	tagName: "li",
	template_el: "#tag-list-item-tpl",
	events: {
		"click .actions .delete": "delete"
	},
	renderAfter: function () {
		$(this.el).addClass("tag");
	},
	delete: function () {
		if (confirm("Supprimer le libellé : " + this.model.get("label") + " ?")) {
			this.model.destroy();
		}
		return false;
	}
});
