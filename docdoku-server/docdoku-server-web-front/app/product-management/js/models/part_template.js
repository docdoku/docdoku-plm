/*global _,$,define,App*/
define([
    'backbone',
    'common-objects/collections/file/attached_file_collection',
    'common-objects/utils/date',
    'common-objects/utils/acl-checker'
], function (Backbone, AttachedFileCollection, Date,ACLChecker) {
    'use strict';
    var Template = Backbone.Model.extend({
        className: 'PartTemplate',

        initialize: function () {
            this.resetAttachedFile();
        },

        resetAttachedFile: function () {
            var fullName = this.get('attachedFile');
            if (fullName) {
                var attachedFile = {
                    fullName: fullName,
                    shortName: _.last(fullName.split('/')),
                    created: true
                };
                this._attachedFile = new AttachedFileCollection(attachedFile);
            } else {
                this._attachedFile = new AttachedFileCollection();
            }
        },

        parse: function (response) {
            return response;
        },

        toJSON: function () {
            return this.clone().set({attributeTemplates: _.reject(this.get('attributeTemplates'),
                function (attribute) {
                    return attribute.name === '';
                }
            )}, {silent: true}).attributes;
        },

        getUploadBaseUrl: function () {
            return App.config.contextPath + '/api/files/' + this.get('workspaceId') + '/part-templates/' + this.get('id') + '/';
        },

        getId: function () {
            return this.get('id');
        },

        getMask: function () {
            return this.get('mask');
        },

        isIdGenerated: function () {
            return this.get('idGenerated');
        },

        getAuthorName: function () {
            return this.get('author').name;
        },

        getAuthorLogin: function () {
            return this.get('author').login;
        },

        getCreationDate: function () {
            return this.get('creationDate');
        },

        getModificationDate: function () {
            return this.get('modificationDate');
        },

        hasAttachedFiles: function(){
            return this._attachedFile.length;
        },
        getFormattedCreationDate: function () {
            return Date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getCreationDate()
            );
        },

        getFormattedModificationDate: function () {
            return Date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getModificationDate()
            );
        },

        getPartType: function () {
            return this.get('partType');
        },

        url: function () {
            if (this.get('id')) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/part-templates/' + this.get('id');
            } else {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/part-templates';
            }
        },

        generateIdUrl: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/part-templates/' + this.get('id') + '/generate_id';
        },

        getBaseName: function () {
            return App.config.workspaceId + '/part-templates/' + this.get('id');
        },

        isAttributesLocked: function () {
            return this.get('attributesLocked');
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
            return ACLChecker.getPermission(this.get('acl'));
        },
        updateACL: function (args) {
            $.ajax({
                type: 'PUT',
                url: this.url()+'/acl',
                data: JSON.stringify(args.acl),
                contentType: 'application/json; charset=utf-8',
                success: args.success,
                error: args.error
            });
        }

    });
    return Template;
});
