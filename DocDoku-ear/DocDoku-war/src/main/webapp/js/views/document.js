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
			"deleteClicked", "deleteItem");
		this.selectedIds = [];
		this.render();
	},
	template: function(data) {
		return Mustache.render(
			$("#document-list-tpl").html(),
			data
		);
	},
	render: function () {
		data = {
			items: this.collection.toJSON()
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
		var model = this.collection.get(modelId)
		model.url = function () { // TODO: Ugly hack to remove
			return "/api/documents/" + app.workspaceId + "/" + model.id;
		};
		model.destroy();
	}
});
