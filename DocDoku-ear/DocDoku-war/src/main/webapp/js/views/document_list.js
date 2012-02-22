var DocumentListView = BaseView.extend({
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
		_.bindAll(this,
			"template", "render",
			"onDocumentListReset", "createDocumentListItemView",
			"itemSelectClicked",
			"new",
			"checkout", "undocheckout", "checkin",
			"delete");
		this.render();
		this.selectedIds = [];
		this.model.documents.bind("reset", this.onDocumentListReset);
		this.model.documents.bind("remove", this.onDocumentListReset);
		this.model.documents.fetch();
	},
	onDocumentListReset: function () {
		_.each(this.views, function (view) {
			view.remove();
		});
		this.views = [];
		this.model.documents.each(this.createDocumentListItemView);
	},
	createDocumentListItemView: function (model) {
		view = new DocumentListItemView({
			model: model
		});
		$(this.el).find("table tbody").append(view.el);
		this.views.push(view);
		view.render();
	},
	render: function () {
		$(this.el).html(this.template({}));
		$(this.el).find(".actions .delete").hide();
	},
	itemSelectClicked: function () {
		if ($(this.el).find("input.select[type=checkbox]").filter(":checked").length > 0) {
			$(this.el).find(".actions .delete").show();
		} else {
			$(this.el).find(".actions .delete").hide();
		}
	},
	new : function () {
		newView = new DocumentNewView({model: this.model});
		newView.render();
		return false;
	},
	checkout: function () {
		_.each(this.views, function (view) {
			view.checkout();
		});
	},
	undocheckout: function () {
		_.each(this.views, function (view) {
			view.undocheckout();
		});
	},
	checkin: function () {
		_.each(this.views, function (view) {
			view.checkin();
		});
	},
	delete: function () {
		_.each(this.views, function (view) {
			view.delete();
		});
		return false;
	}
});
