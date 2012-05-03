define([
	"collections/folder_document",
	"views/content_document_list",
	"views/document_new",
	"text!templates/folder_document_list.html"
], function (
	FolderDocumentList,
	ContentDocumentListView,
	DocumentNewView,
	template
) {
	var FolderDocumentListView = ContentDocumentListView.extend({
		template: Mustache.compile(template),
		collection: function () {
			return new FolderDocumentList();
		},
		initialize: function () {
			ContentDocumentListView.prototype.initialize.apply(this, arguments);
			this.events["click .actions .new-document"] = "actionNew";
			if (this.model) {
				this.collection.parent = this.model;
			}
		},
		actionNew: function () {
			var view = this.addSubView(
				new DocumentNewView({
					collection: this.collection
				})
			).show();
			return false;
		},
	});
	return FolderDocumentListView;
});
