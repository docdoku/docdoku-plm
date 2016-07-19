/*global define,App*/
define([
    'collections/task_document',
    'views/content_document_list',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/release_button.html',
    'text!common-objects/templates/buttons/obsolete_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!templates/search_document_form.html',
    'text!templates/status_filter.html',
    'text!templates/task_document_list.html'
], function (TaskDocumentList, ContentDocumentListView, deleteButton, checkoutButtonGroup, tagsButton, newVersionButton, releaseButton, obsoleteButton, aclButton, searchForm, statusFilter, template) {
	'use strict';
	var TaskDocumentListView = ContentDocumentListView.extend({

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
            statusFilter: statusFilter
        },

        collection: function () {
            var c = new TaskDocumentList();
            c.setFilterStatus(this.options.filter);
            return c;
        },

        initialize: function () {
            ContentDocumentListView.prototype.initialize.apply(this, arguments);
            if (this.model) {
                this.collection.parent = this.model;
            }
            this.events['click button[value="all"]'] = 'allClicked';
            this.events['click button[value="in_progress"]'] = 'inProgressClicked';
        },
        rendered: function () {
            ContentDocumentListView.prototype.rendered.apply(this, arguments);
            var filter = this.options.filter || 'all';
            this.$('button.filter[value=' + filter + ']').addClass('active');
        },
        allClicked: function () {
            window.location.hash = '#' + App.config.workspaceId + '/tasks';
        },
        inProgressClicked: function () {
            window.location.hash = '#' + App.config.workspaceId + '/tasks/in_progress';
        }

    });
    return TaskDocumentListView;
});
