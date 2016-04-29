/*global _,$,define,App*/
define([
    'backbone',
    'common-objects/utils/date',
    'common-objects/utils/acl-checker'
], function (Backbone, Date, ACLChecker) {
	'use strict';
    var ChangeItemModel = Backbone.Model.extend({
        priorities: {
            LOW: 'LOW',
            MEDIUM: 'MEDIUM',
            HIGH: 'HIGH',
            EMERGENCY: 'EMERGENCY'
        },

        categories: {
            ADAPTIVE: 'ADAPTIVE',
            CORRECTIVE: 'CORRECTIVE',
            PERFECTIVE: 'PERFECTIVE',
            PREVENTIVE: 'PREVENTIVE',
            OTHER: 'OTHER'
        },

        initialize: function () {
            _.bindAll(this);
        },

        getId: function () {
            return this.get('id');
        },

        getName: function () {
            return this.get('name');
        },

        getAuthor: function () {
            return this.get('author');
        },

        getAuthorName: function () {
            return this.get('authorName');
        },

        getAssignee: function () {
            return this.get('assignee');
        },

        getAssigneeName: function () {
            return this.get('assigneeName');
        },

        getCreationDate: function () {
            return this.get('creationDate');
        },

        getFormattedCreationDate: function () {
            return Date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getCreationDate()
            );
        },

        getDescription: function () {
            return this.get('description');
        },

        getPriority: function () {
            return this.get('priority');
        },

        getCategory: function () {
            return this.get('category');
        },

        getAffectedDocuments: function () {
            return this.get('affectedDocuments');
        },

        getAffectedParts: function () {
            return this.get('affectedParts');
        },

        getTags: function () {
            return this.get('tags');
        },

        addTags: function (tags) {
            $.ajax({
                context: this,
                type: 'POST',
                url: this.url() + '/tags',
                data: JSON.stringify({tags:tags}),
                contentType: 'application/json; charset=utf-8',
                success: function () {
                }
            });
        },

        removeTag: function (tag, callback) {
            $.ajax({
                type: 'DELETE',
                url: this.url() + '/tags/' + tag,
                success: function () {
                    callback();
                }
            });
        },

        removeTags: function (tags, callback) {
            var baseUrl = this.url() + '/tags/';
            var count = 0;
            var total = _(tags).length;
            _(tags).each(function (tag) {
                $.ajax({
                    type: 'DELETE',
                    url: baseUrl + tag,
                    success: function () {
                        count++;
                        if (count >= total) {
                            callback();
                        }
                    }
                });
            });

        },

        saveAffectedDocuments: function (documents, callback) {
            $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/affected-documents',
                data: JSON.stringify({documents:documents}),
                contentType: 'application/json; charset=utf-8',
                success: function () {
                    if (callback) {
                        callback();
                    }
                }
            });
        },

        saveAffectedParts: function (parts, callback) {
            $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/affected-parts',
                data: JSON.stringify({parts:parts}),
                contentType: 'application/json; charset=utf-8',
                success: function () {
                    if (callback) {
                        callback();
                    }
                }
            });
        },

        getACL: function () {
            return this.get('acl');
        },

        updateACL: function (args) {
            $.ajax({
                type: 'PUT',
                url: this.url() + '/acl',
                data: JSON.stringify(args.acl),
                contentType: 'application/json; charset=utf-8',
                success: args.success,
                error: args.error
            });
        },

        hasACLForCurrentUser: function () {
            return this.getACLPermissionForCurrentUser() !== false;
        },

        isForbidden: function () {
            return this.getACLPermissionForCurrentUser() === 'FORBIDDEN';
        },

        isReadOnly: function () {
            return this.getACLPermissionForCurrentUser() === 'READ_ONLY';
        },

        isFullAccess: function () {
            return this.getACLPermissionForCurrentUser() === 'FULL_ACCESS';
        },

        getACLPermissionForCurrentUser: function () {
            return ACLChecker.getPermission(this.getACL());
        },

        isWritable: function () {
            return this.get('writable');
        }

    });

    return ChangeItemModel;
});
