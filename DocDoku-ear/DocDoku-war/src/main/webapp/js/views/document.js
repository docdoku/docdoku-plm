var DocumentListView = BaseView.extend({
	tagName: "div",
	template_el: "#document-list-tpl",
	events: {
		"click tbody tr input": "itemSelectClicked",
		"click .actions .new": "new",
		"click .actions .delete": "delete"
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"itemSelectClicked",
			"new",
			"delete", "deleteItem", "itemDeleted");
		this.selectedIds = [];
		this.model.documents.bind("reset", this.render);
		this.model.documents.fetch({data: {path:this.model.path()}});
	},
	render: function () {
		data = {
			items: this.model.documents.toJSON()
		}
		_.each(data.items, function (item) {
			// Format date
			if (item.lastIterationDate) {
				item.lastIterationDate = new Date(item.lastIterationDate).toLocaleDateString();
			}
			// checkOut
			item.checkOutDate = item.checkOutDate ?
				new Date(item.lastIterationDate).toLocaleDateString() :
				"";
		});
		$(this.el).html(this.template(data));
		$(this.el).find(".actions .delete").hide();
	},
	itemSelectClicked: function (ev) {
		modelId = $(ev.target).val();
		pos = _.indexOf(this.selectedIds, modelId);
		if (pos == -1) {
			this.selectedIds.push(modelId);
		} else {
			this.selectedIds.pop(pos);
		}
		if (this.selectedIds.length > 0) {
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
	delete: function () {
		_.each(this.selectedIds, this.deleteItem);
		return false;
	},
	deleteItem: function (modelId) {
		var model = this.model.documents.get(modelId);
		if (model.checkOutDate) {
			// Do not try to delete reserved documents
			this.selectedIds.pop(_.indexOf(this.selectedIds, modelId));
		} else {
			model.destroy({success: this.itemDeleted});
		}
	},
	itemDeleted: function (model) {
		this.selectedIds.pop(_.indexOf(this.selectedIds, model.documentId));
		// Refresh when all deletion are done
		if (this.selectedIds.length == 0) {
			this.model.documents.fetch({data: {path:this.model.path()}});
		}
	}
});
DocumentNewView = Backbone.View.extend({
	tagName: "div",
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
	template: function(data) {
		return Mustache.render(
			$("#document-new-tpl").html(),
			data
		);
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
				path: app.restpath(this.model.get("completePath"))
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
		this.model.documents.fetch({data: {path:this.model.path()}});
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
