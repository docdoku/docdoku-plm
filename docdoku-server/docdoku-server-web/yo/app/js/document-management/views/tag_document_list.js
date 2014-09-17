/*global define,App*/
define([
    'collections/tag_document',
    'views/content_document_list',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!templates/search_document_form.html',
    'text!templates/tag_document_list.html'
], function (TagDocumentList, ContentDocumentListView, deleteButton, checkoutButtonGroup, tagsButton, newVersionButton, aclButton, searchForm, template) {
	'use strict';
	var TagDocumentListView = ContentDocumentListView.extend({

        template: template,

        partials: {
            deleteButton: deleteButton,
            checkoutButtonGroup: checkoutButtonGroup,
            tagsButton: tagsButton,
            newVersionButton: newVersionButton,
            searchForm: searchForm,
            aclButton: aclButton
        },

        collection: function () {
            return new TagDocumentList();

        },
        initialize: function () {
            ContentDocumentListView.prototype.initialize.apply(this, arguments);
            if (this.model) {
                this.collection.parent = this.model;
            }
            this.templateExtraData = {
                isReadOnly: App.appView.isReadOnly()
            };
        }
    });
    return TagDocumentListView;
});
