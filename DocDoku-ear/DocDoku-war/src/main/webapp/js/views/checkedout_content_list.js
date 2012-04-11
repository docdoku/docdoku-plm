var CheckedoutContentListView = ContentDocumentListView.extend({
	collection: function () { return new CheckedoutDocumentList(); },
});
