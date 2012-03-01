var DocumentCheckedoutListView = ListView.extend({
	ItemView: DocumentCheckedoutListItemView,
	tagName: "div",
	template_el: "#document-list-tpl",
	events: {
		"click tbody tr input": "itemSelectClicked",
		"click .actions .new": "new",
		"click .actions .checkout": "checkout",
		"click .actions .undocheckout": "undocheckout",
		"click .actions .checkin": "checkin",
		"click .actions .delete": "delete"
	},
	initialize: function () {
		this.documentCheckedoutListViewBindings();
		this.collection.fetch();
	},
	documentCheckedoutListViewBindings: function () {
		this.listViewBindings();
		_.bindAll(this,
			"itemSelectClicked",
			"new",
			"checkout", "undocheckout", "checkin",
			"delete");
	},
	renderAfter: function () {
		$(this.el).find(".actions .checkout").remove();
	},
	itemSelectClicked: function () {
		if ($(this.el).find("input.select[type=checkbox]").filter(":checked").length > 0) {
			$(this.el).find(".actions .delete").show();
			$(this.el).find(".actions .checkout-group").show();
		} else {
			$(this.el).find(".actions .delete").hide();
			$(this.el).find(".actions .checkout-group").hide();
		}
	},
	new : function () {
		var view = new DocumentNewView({model: this.model});
		view.render();
		return false;
	},
	checkout: function () {
		_.each(this.itemViews, function (view) {
			view.checkout();
		});
	},
	undocheckout: function () {
		_.each(this.itemViews, function (view) {
			view.undocheckout();
		});
	},
	checkin: function () {
		_.each(this.itemViews, function (view) {
			view.checkin();
		});
	},
	delete: function () {
		_.each(this.itemViews, function (view) {
			view.delete();
		});
		return false;
	}
});
