define([
	"collections/checkedout_document",
	"views/content_document_list"
], function (
	CheckedoutDocumentList,
	ContentDocumentListView
) {
	var CheckedoutContentListView = ContentDocumentListView.extend({
		collection: function () {
			return new CheckedoutDocumentList();
		}
	});
	return CheckedoutContentListView;
});
