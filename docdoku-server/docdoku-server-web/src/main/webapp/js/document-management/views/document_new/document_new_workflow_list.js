define([
	"collections/workflow",
	"views/base",
	"text!templates/document_new/document_new_workflow_select.html"
], function (
	WorkflowList,
	BaseView,
	template
) {
	var DocumentNewWorkflowListView = BaseView.extend({
		template: Mustache.compile(template),
		collection: function () {
			return WorkflowList.getInstance();
		},
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
	return DocumentNewWorkflowListView;
});
