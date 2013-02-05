define([
	"collections/tag_document",
	"views/content_document_list",
    "text!templates/content_document_list_checkout_button_group.html",
    "text!templates/content_document_list_tags_button.html",
    "text!templates/search_document_form.html",
    "text!templates/tag_document_list.html"
], function (
	TagDocumentList,
	ContentDocumentListView,
    checkout_button_group,
    tags_button,
    search_form,
    template
) {
	var TagDocumentListView = ContentDocumentListView.extend({

        template: Mustache.compile(template),

        partials: {
            checkout_button_group: checkout_button_group,
            tags_button:tags_button,
            search_form:search_form
        },

		collection: function () {
            return new TagDocumentList();

		},
		initialize: function () {
			ContentDocumentListView.prototype.initialize.apply(this, arguments);
			if (this.model) {
				this.collection.parent = this.model;
			}
		}
    });
	return TagDocumentListView;
});
