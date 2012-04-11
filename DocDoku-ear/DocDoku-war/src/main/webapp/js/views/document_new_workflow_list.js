var DocumentNewWorkflowListView = BaseView.extend({
	collection: function () { return WorkflowList.getInstance(); },
	template: "document-new-workflow-select-tpl",
	collectionReset: function () {
		this.render();
	},
	collectionToJSON: function () {
		var data = BaseView.prototype.collectionToJSON.call(this);
		data.unshift({
			id: ""
		});
		return data;
	},
	selected: function () {
		var id = $("#select-" + this.cid).val();
		var model = this.collection.get(id);
		return model;
	},
});
