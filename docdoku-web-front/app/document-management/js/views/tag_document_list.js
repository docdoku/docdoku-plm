/*global define,App*/
define([
    'collections/tag_document',
    'views/content_document_list',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/release_button.html',
    'text!common-objects/templates/buttons/obsolete_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!templates/search_document_form.html',
    'text!templates/tag_document_list.html',
    'views/document/document_new'
], function (TagDocumentList, ContentDocumentListView, deleteButton, checkoutButtonGroup, tagsButton, newVersionButton, releaseButton, obsoleteButton, aclButton, searchForm, template, DocumentNewView) {
	'use strict';
	var TagDocumentListView = ContentDocumentListView.extend({

        template: template,

        partials: {
            deleteButton: deleteButton,
            checkoutButtonGroup: checkoutButtonGroup,
            tagsButton: tagsButton,
            newVersionButton: newVersionButton,
            releaseButton: releaseButton,
            obsoleteButton:obsoleteButton,
            searchForm: searchForm,
            aclButton: aclButton
        },

        collection: function () {
            return new TagDocumentList();

        },
        initialize: function () {
            ContentDocumentListView.prototype.initialize.apply(this, arguments);
            this.events['click .actions .new-document'] = 'actionNew';

            if (this.model) {
                this.collection.parent = this.model;
            }
            this.templateExtraData = {
                isReadOnly: App.appView.isReadOnly()
            };
        },
        actionNew: function () {
            this.addSubView(
                new DocumentNewView({
                    collection: this.collection,
                    autoAddTag:this.model
                })
            ).show();
            return false;
        }
    });
    return TagDocumentListView;
});
