var DocumentListView = Backbone.View.extend({
	tagName: "div",
	events: {
		"click tbody tr input": "itemSelectClicked",
		"click .actions .delete": "deleteClicked"
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"itemSelectClicked",
			"itemDeleted",
			"deleteClicked", "deleteItem");
		this.selectedIds = [];
		this.model.documents.bind("reset", this.render);
		this.model.documents.fetch();
	},
	template: function(data) {
		return Mustache.render(
			$("#document-list-tpl").html(),
			data
		);
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
	deleteClicked: function () {
		_.each(this.selectedIds, this.deleteItem);
		return false;
	},
	deleteItem: function (modelId) {
		var model = this.model.documents.get(modelId);
		if (model.checkOutDate) {
			// Do not try to delete reserved documents
			this.selectedIds.pop(_.indexOf(this.selectedIds, modelId));
		} else {
			var revision = new DocumentRevision({
				id: model.get("version")
			});
			revision.urlRoot = "/api/documents/" + app.workspaceId + "/" + model.id;
			revision.documentId = modelId;
			revision.destroy({success: this.itemDeleted});
		}
	},
	itemDeleted: function (model) {
		this.selectedIds.pop(_.indexOf(this.selectedIds, model.documentId));
		// Refresh when all deletion are done
		if (this.selectedIds.length == 0) {
			this.model.documents.fetch();
		}
	}
});
