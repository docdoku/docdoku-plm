/*global define*/
define([
    "collections/task_document",
    "views/content_document_list",
    "text!common-objects/templates/buttons/delete_button.html",
    "text!common-objects/templates/buttons/checkout_button_group.html",
    "text!common-objects/templates/buttons/tags_button.html",
    "text!common-objects/templates/buttons/new_version_button.html",
    "text!common-objects/templates/buttons/ACL_button.html",
    "text!templates/search_document_form.html",
    "text!templates/status_filter.html",
    "text!templates/task_document_list.html"
], function (TaskDocumentList, ContentDocumentListView, delete_button, checkout_button_group, tags_button, new_version_button, ACL_button, search_form, status_filter, template) {
    var TaskDocumentListView = ContentDocumentListView.extend({

        template: template,

        partials: {
            delete_button: delete_button,
            checkout_button_group: checkout_button_group,
            tags_button: tags_button,
            new_version_button: new_version_button,
            search_form: search_form,
            ACL_button: ACL_button,
            status_filter: status_filter
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
            this.events['click button[value="all"]'] = "allClicked";
            this.events['click button[value="in_progress"]'] = "inProgressClicked";
        },
        rendered: function () {
            ContentDocumentListView.prototype.rendered.apply(this, arguments);
            var filter = this.options.filter || "all";
            this.$("button.filter[value=" + filter + "]").addClass("active");
        },
        allClicked: function () {
            window.location.hash = '#' + APP_CONFIG.workspaceId + '/tasks';
        },
        inProgressClicked: function () {
            window.location.hash = '#' + APP_CONFIG.workspaceId + '/tasks/in_progress';
        }

    });
    return TaskDocumentListView;
});
