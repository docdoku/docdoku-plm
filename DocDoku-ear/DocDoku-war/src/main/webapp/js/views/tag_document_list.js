var TagDocumentListView = ContentDocumentListView.extend({
	collection: TagDocumentList,
	initialize: function () {
		ContentDocumentListView.prototype.initialize.apply(this, arguments);
		if (this.model) {
			this.collection.parent = this.model;
		}
	},
});
