define([
	"collections/checked_out_document",
	"views/content_document_list",
    "text!common-objects/templates/buttons/delete_button.html",
    "text!common-objects/templates/buttons/checkout_button_group.html",
    "text!common-objects/templates/buttons/tags_button.html",
    "text!common-objects/templates/buttons/new_version_button.html",
    "text!common-objects/templates/buttons/ACL_button.html",
    "text!templates/search_document_form.html",
    "text!templates/checked_out_document_list.html"
], function (
    CheckedOutDocumentList,
	ContentDocumentListView,
    delete_button,
    checkout_button_group,
    tags_button,
    new_version_button,
    ACL_button,
    search_form,
    template
) {
	var CheckedOutDocumentListView = ContentDocumentListView.extend({

        template: Mustache.compile(template),

        partials: {
            delete_button: delete_button,
            checkout_button_group: checkout_button_group,
            tags_button:tags_button,
            new_version_button: new_version_button,
            search_form:search_form,
            ACL_button:ACL_button
        },

		collection: function () {
            return new CheckedOutDocumentList();

		},
		initialize: function () {
			ContentDocumentListView.prototype.initialize.apply(this, arguments);
			if (this.model) {
				this.collection.parent = this.model;
			}
		}
    });
	return CheckedOutDocumentListView;
});
