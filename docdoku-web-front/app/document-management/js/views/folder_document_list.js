/*global define*/
define([
    'collections/folder_document',
    'views/content_document_list',
    'views/document/document_new',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/import_button.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/release_button.html',
    'text!common-objects/templates/buttons/obsolete_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!common-objects/templates/buttons/snap_in_progress_button.html',
    'text!templates/search_document_form.html',
    'text!templates/folder_document_list.html'
], function (FolderDocumentList, ContentDocumentListView, DocumentNewView, deleteButton, checkoutButtonGroup, tagsButton, importButton, newVersionButton, releaseButton, obsoleteButton, aclButton, snapInProgressButton, searchForm, template) {
    'use strict';
	var FolderDocumentListView = ContentDocumentListView.extend({

        template: template,

        partials: {
            deleteButton: deleteButton,
            checkoutButtonGroup: checkoutButtonGroup,
            tagsButton: tagsButton,
            newVersionButton: newVersionButton,
            releaseButton: releaseButton,
            obsoleteButton:obsoleteButton,
            searchForm: searchForm,
            aclButton: aclButton,
            snapInProgressButton: snapInProgressButton,
            importButton: importButton
        },
        collection: function () {
            return new FolderDocumentList();
        },
        initialize: function () {
            ContentDocumentListView.prototype.initialize.apply(this, arguments);
            this.events['click .actions .new-document'] = 'actionNew';
            if (this.model) {
                this.collection.parent = this.model;
            }
        },

        actionNew: function () {
            this.addSubView(
                new DocumentNewView({
                    collection: this.collection
                })
            ).show();
            return false;
        }
    });
    return FolderDocumentListView;
});
