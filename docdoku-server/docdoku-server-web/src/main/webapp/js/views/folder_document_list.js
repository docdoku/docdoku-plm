define([
	"collections/folder_document",
	"views/content_document_list",
	"views/document_new/document_new",
	"text!templates/content_document_list_checkout_button_group.html",
	"text!templates/folder_document_list.html"
], function (
	FolderDocumentList,
	ContentDocumentListView,
	DocumentNewView,
	checkout_button_group,
	template
) {
	var FolderDocumentListView = ContentDocumentListView.extend({
		template: Mustache.compile(template),
		partials: {
			checkout_button_group: checkout_button_group
		},
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
		}
	});
	return FolderDocumentListView;
});
