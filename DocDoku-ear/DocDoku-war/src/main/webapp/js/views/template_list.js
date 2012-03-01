var TemplateListView = ListView.extend({
	ItemView: TemplateListItemView,
	tagName: "div",
	template_el: "#template-list-tpl",
	events: {
		"click tbody tr input": "itemSelectClicked",
		"click .actions .new": "new",
		"click .actions .delete": "delete"
	},
	initialize: function () {
		this.templateListViewBindings();
		this.collection.fetch();
	},
	templateListViewBindings: function () {
		this.listViewBindings();
		_.bindAll(this, "itemSelectClicked", "new", "delete");
	},
	itemSelectClicked: function () {
		if ($(this.el).find("input.select[type=checkbox]").filter(":checked").length > 0) {
			$(this.el).find(".actions .delete").show();
		} else {
			$(this.el).find(".actions .delete").hide();
		}
	},
	new : function () {
		var newView = new TemplateNewView({model: this.model});
		newView.render();
		return false;
	},
	delete: function () {
		_.each(this.itemViews, function (view) {
			view.delete();
		});
		return false;
	}
});
