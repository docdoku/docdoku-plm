var DocumentCheckedoutListView = ListView.extend({
	ItemView: DocumentListItemView,
	tagName: "div",
	template_el: "#document-list-tpl",
	events: {
		"click thead input.select": "selectAll",
		"click tbody tr input": "itemSelectClicked",
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
			"itemSelectClicked", "itemSelectClicked",
			"undocheckout", "checkin", "delete");
	},
	renderAfter: function () {
		$(this.el).find(".actions .new").remove();
		$(this.el).find(".actions .checkout").remove();
	},
	selectAll: function () {
		var elSelectList = $(this.el).find("tbody input.select")
		if ($(this.el).find("thead input.select").first().is(":checked")) {
			elSelectList.each(function () {
				$(this).attr("checked", true);
			});
		} else {
			elSelectList.each(function () {
				$(this).attr("checked", false);
			});
		}
		this.itemSelectClicked();
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
