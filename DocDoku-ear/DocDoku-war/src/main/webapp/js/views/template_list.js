var TemplateListView = BaseView.extend({
	tagName: "div",
	template_el: "#template-list-tpl",
	events: {
		"click tbody tr input": "itemSelectClicked",
		"click .actions .new": "new",
		"click .actions .delete": "delete"
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"onTemplateListReset", "createTemplateListItemView",
			"itemSelectClicked",
			"new",
			"checkout", "undocheckout", "checkin",
			"delete");
		this.render();
		this.selectedIds = [];
		this.collection.bind("reset", this.onTemplateListReset);
		this.collection.bind("remove", this.onTemplateListReset);
		this.collection.fetch();
	},
	onTemplateListReset: function () {
		_.each(this.views, function (view) {
			view.remove();
		});
		this.views = [];
		this.collection.each(this.createTemplateListItemView);
	},
	createTemplateListItemView: function (model) {
		view = new TemplateListItemView({
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
		newView = new TemplateNewView({model: this.model});
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
