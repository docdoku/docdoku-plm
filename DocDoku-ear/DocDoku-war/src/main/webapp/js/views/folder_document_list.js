var FolderDocumentListView = ContentDocumentListView.extend({
	collection: FolderDocumentList,
	template: "folder-document-list-tpl",
	initialize: function () {
		ContentDocumentListView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"click .actions .new-document": "actionNew",
		});
		if (this.model) {
			this.collection.parent = this.model;
		}
	},
	actionNew: function () {
		var view = this.addSubView(new DocumentNewView({
			collection: this.collection
		}));
		view.show();
	},
});
