define([
	"collections/tag_document",
	"views/content_document_list"
], function (
	TagDocumentList,
	ContentDocumentListView
) {
	var TagDocumentListView = ContentDocumentListView.extend({
		collection: function () {
			return new TagDocumentList();
		},
		initialize: function () {
			ContentDocumentListView.prototype.initialize.apply(this, arguments);
			if (this.model) {
				this.collection.parent = this.model;
			}
		},
	});
	return TagDocumentListView;
});
