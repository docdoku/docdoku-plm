/*global _,$,define,App,window*/
define(['backbone', 'common-objects/utils/date', 'common-objects/collections/part_iteration_collection','common-objects/utils/acl-checker'],
function (Backbone, Date, PartIterationList, ACLChecker) {
    'use strict';

    var Part = Backbone.Model.extend({
        idAttribute: 'partKey',

        initialize: function () {
            _.bindAll(this);
        },

        parse: function (data) {
            this.iterations = new PartIterationList(data.partIterations);
            this.iterations.setPart(this);
            delete data.partIterations;
            delete data.partList;
            return data;
        },

        init: function (number, version) {
            this.set('number', number);
            this.set('version', version);
            return this;
        },

        getNumber: function () {
            return this.get('number');
        },

        getType: function () {
            return this.get('type');
        },

        getName: function () {
            return this.get('name');
        },

        getVersion: function () {
            return this.get('version');
        },

        getDescription: function () {
            return this.get('description');
        },

        getPartKey: function () {
            return this.get('partKey');
        },

        getWorkspace: function () {
            return this.get('workspaceId');
        },

        getCheckoutUser: function () {
            return this.get('checkOutUser');
        },

        isReleased: function () {
            return this.get('status') === 'RELEASED';
        },

        getFormattedCheckoutDate: function () {
            if (this.isCheckout()) {
                return Date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    this.getCheckoutDate()
                );
            }
            return null;
        },

        getFormattedCreationDate: function () {
            return Date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getCreationDate()
            );
        },

        getCheckoutDate: function () {
            return this.get('checkOutDate');
        },

        getCreationDate: function () {
            return this.get('creationDate');
        },


        isCheckoutByConnectedUser: function () {
            return this.isCheckout() ? this.getCheckOutUserLogin() === App.config.login : false;
        },

        getUrl: function () {
            return this.url();
        },

        hasIterations: function () {
            return !this.getIterations().isEmpty();
        },

        getLastIteration: function () {
            return this.getIterations().last();
        },

        isLastIteration: function (iterationNumber) {
            // return TRUE if the iteration is the very last (check or uncheck)
            return this.get('lastIterationNumber') === iterationNumber;
        },

        getIterations: function () {
            return this.iterations;
        },

        getAuthorLogin: function () {
            return this.get('author').login;
        },

        getAuthorName: function () {
            return this.get('author').name;
        },

        getAuthor: function () {
            return this.get('author').name;
        },

        getCheckOutUserName: function () {
            if (this.isCheckout()) {
                return this.getCheckoutUser().name;
            }
            return null;
        },

        getCheckOutUserLogin: function () {
            if (this.isCheckout()) {
                return this.getCheckoutUser().login;
            }
            return null;
        },

        isStandardPart: function () {
            return this.get('standardPart') ? 1 : 0;
        },

        isStandardPartReadable: function () {
            return this.get('standardPart') ? App.config.i18n.TRUE : App.config.i18n.FALSE;
        },

        getLifeCycleState: function () {
            return this.get('lifeCycleState');
        },

        isAttributesLocked: function () {
            return this.get('attributesLocked');
        },

        checkout: function () {
            return $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/checkout',
                success: function () {
                    this.fetch();
                }
            });
        },

        undocheckout: function () {
            return $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/undocheckout',
                success: function () {
                    this.fetch();
                },
                error: function (xhr) {
                    window.alert(xhr.responseText);
                }
            });
        },

        checkin: function () {
            return $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/checkin',
                success: function () {
                    this.fetch();
                }
            });
        },

        isCheckout: function () {
            return !_.isNull(this.get('checkOutDate'));
        },

        getPermalink: function () {
            return encodeURI(
                    window.location.origin +
                    App.config.contextPath +
                    '/parts/' +
                    this.getWorkspace() +
                    '/' +
                    this.getNumber() +
                    '/' +
                    this.getVersion()
            );
        },

        createShare: function (args) {
            $.ajax({
                type: 'POST',
                url: this.url() + '/share',
                data: JSON.stringify(args.data),
                contentType: 'application/json; charset=utf-8',
                success: args.success
            });
        },

        publish: function (args) {
            $.ajax({
                type: 'PUT',
                url: this.url() + '/publish',
                success: args.success
            });
        },

        unpublish: function (args) {
            $.ajax({
                type: 'PUT',
                url: this.url() + '/unpublish',
                success: args.success
            });
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
            return ACLChecker.getPermission(this.get('acl'));
        },

        createNewVersion: function (description, workflow, roleMappingList, aclList) {

            var data = {
                description: description,
                workflowModelId: workflow ? workflow.get('id') : null,
                roleMapping: workflow ? roleMappingList : null,
                acl: aclList
            };

            $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/newVersion',
                data: JSON.stringify(data),
                contentType: 'application/json; charset=utf-8',
                success: function () {
                    this.collection.fetch({reset: true});
                }
            });
        },

        release: function () {
            $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/release',
                success: function () {
                    this.fetch();
                }
            });
        },

        url: function () {
            if (this.getPartKey()) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.getPartKey();
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/';
        }


    });

    return Part;

});
