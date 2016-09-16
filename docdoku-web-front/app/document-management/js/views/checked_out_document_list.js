/*global define*/
define([
    'collections/checked_out_document',
    'views/content_document_list',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!common-objects/templates/buttons/snap_in_progress_button.html',
    'text!templates/search_document_form.html',
    'text!templates/checked_out_document_list.html'
], function (CheckedOutDocumentList, ContentDocumentListView, deleteButton, checkoutButtonGroup, tagsButton, newVersionButton, aclButton, snapInProgressButton, searchForm, template) {
    'use strict';
	var CheckedOutDocumentListView = ContentDocumentListView.extend({

        template: template,

        partials: {
            deleteButton: deleteButton,
            checkoutButtonGroup: checkoutButtonGroup,
            tagsButton: tagsButton,
            searchForm: searchForm,
            aclButton: aclButton,
            snapInProgressButton: snapInProgressButton
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
