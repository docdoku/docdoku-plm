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
		this.model.documents.fetch({data: {path:this.model.get("path")}});
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
var DocumentListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#document-list-item-tpl",
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"isSelected",
			"checkout", "undocheckout", "checkin",
			"delete");
			this.model.bind("change", this.render);
	},
	render: function () {
		data = this.model.toJSON();
		// Format date
		if (data.lastIterationDate) {
			data.lastIterationDate = new Date(data.lastIterationDate).toLocaleDateString();
		}
		if (data.checkOutDate) {
			data.checkOutDate = new Date(data.lastIterationDate).toLocaleDateString();
		}
		$(this.el).html(this.template({
			model: data
		}));
	},
	isSelected: function () {
		return $(this.el).find("input.select").filter(":checked").length > 0;
	},
	checkout: function () {
		if (this.isSelected()) {
			this.model.checkout();
		}
	},
	undocheckout: function () {
		if (this.isSelected()) {
			this.model.undocheckout();
		}
	},
	checkin: function () {
		if (this.isSelected()) {
			this.model.checkin();
		}
	},
	delete: function () {
		if (this.isSelected()) {
			this.model.destroy();
		}
	}
});
DocumentNewView = BaseView.extend({
	tagName: "div",
	template_el: "#document-new-tpl",
	events: {
		"submit form": "create",
		"click .create": "create",
		"click .cancel": "cancel",
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"create", "cancel",
			"success", "error");
	},
	render: function () {
		$(this.el).html(this.template({}));
		$(this.el).modal("show");
		$(this.el).find("input.id").first().focus();
	},
	create: function () {
		var reference = $(this.el).find("input.reference").first().val();
		if (reference) {
			newDocument = new Document({
				reference: reference,
				title: $(this.el).find("input.title").first().val(),
				description: $(this.el).find("textarea.description").first().val(),
				path: app.restpath(this.model.completePath())
			})
			newDocument.urlRoot = "/api/documents/" + app.workspaceId;
			newDocument.bind("sync", this.success);
			newDocument.bind("error", this.error);
			newDocument.save();
		}
		return false;
	},
	success: function () {
		$(this.el).modal("hide");
		this.model.documents.fetch({data: {path:this.model.get("path")}});
		this.remove();
	},
	error: function (model, error) {
		if (error.responseText) {
			alert(error.responseText);
		} else {
			console.error(error);
		}
	},
	cancel: function () {
		$(this.el).modal("hide");
		this.remove();
		return false;
	}
});
