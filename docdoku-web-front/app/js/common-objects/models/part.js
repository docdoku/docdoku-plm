/*global _,$,define,App,window*/
define([
        'backbone',
        'common-objects/utils/date',
        'common-objects/collections/part_iteration_collection',
        'common-objects/collections/modification_notification_collection',
        'common-objects/utils/acl-checker'
    ],
    function (Backbone, Date, PartIterationList, ModificationNotificationCollection, ACLChecker) {
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
                this.modificationNotifications = new ModificationNotificationCollection(data.notifications);
                delete data.notifications;
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

            getTags: function () {
                return this.get('tags');
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

            isObsolete : function(){
                return this.get('status') === 'OBSOLETE';
            },

            getObsoleteDate: function() {
                return Date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    this.get('obsoleteDate')
                );
            },

            getObsoleteAuthor: function() {
                return this.get('obsoleteAuthor');
            },

            getObsoleteAuthorLogin: function () {
                if (this.isObsolete()) {
                    return this.getObsoleteAuthor().login;
                }
                return null;
            },

            getObsoleteAuthorName: function () {
                if (this.isObsolete()) {
                    return this.getObsoleteAuthor().name;
                }
                return null;
            },

            getReleaseDate: function() {
                return Date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    this.get('releaseDate')
                );
            },

            getReleaseAuthor: function() {
                return this.get('releaseAuthor');
            },

            getReleaseAuthorLogin: function () {
                if (this.getReleaseAuthor()) {
                    return this.getReleaseAuthor().login;
                }
                return null;
            },

            getReleaseAuthorName: function () {
                if (this.getReleaseAuthor()) {
                    return this.getReleaseAuthor().name;
                }
                return null;
            },

            getFormattedCheckoutDate: function () {
                return Date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    this.getCheckoutDate()
                );
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

            getCheckoutDate: function () {
                return this.get('checkOutDate');
            },

            getCreationDate: function () {
                return this.get('creationDate');
            },

            getModificationDate: function () {
                var lastIteration = this.getLastIteration();
                if (lastIteration) {
                    return lastIteration.get('modificationDate');
                }
                return null;
            },

            getRevisionDate: function () {
                var lastIteration = this.getLastIteration();
                if (this.isCheckout()) {
                    return lastIteration.get('creationDate');
                } else {
                    return lastIteration.get('checkInDate');
                }
            },


            isCheckoutByConnectedUser: function () {
                return this.isCheckout() ? this.getCheckOutUserLogin() === App.config.login : false;
            },

            isLocked:function(){
                return this.isCheckout() && !this.isCheckoutByConnectedUser();
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

            isLastIterationAssembly: function () {
                if (this.hasIterations()) {
                    return this.getLastIteration().isAssembly();
                }
                return false;
            },

            hasLastIterationAttachedFiles: function () {
                if (this.hasIterations()) {
                    return this.getLastIteration().getAttachedFiles().length > 0 || this.getLastIteration().get('nativeCADFile');
                }
                return false;
            },

            getIterations: function () {
                return this.iterations;
            },

            hasModificationNotifications: function () {
                return this.modificationNotifications && this.modificationNotifications.models.length;
            },

            hasUnreadModificationNotifications: function () {
                return _.select(this.modificationNotifications.models || [], function(notif) {
                    return !notif.isAcknowledged();
                }).length;
            },

            getModificationNotifications: function () {
                return this.modificationNotifications;
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

            getDisplayKey: function () {
                if (this.getName()) {
                    return this.getName() + ' < ' + this.getNumber() + '-' + this.getVersion() + ' >';
                }
                return '< ' + this.getNumber() + '-' + this.getVersion() + ' >';
            },

            getUsedByProductInstances: function (args) {
                $.ajax({
                    type: 'GET',
                    url: this.getUrl() + '/used-by-product-instance-masters',
                    success: args.success,
                    error: args.error
                });
            },

            getUsedByPartsAsComponent: function (args) {
                $.ajax({
                    type: 'GET',
                    url: this.getUrl() + '/used-by-as-component',
                    success: args.success,
                    error: args.error
                });
            },

            getUsedByPartsAsSubstitute: function (args) {
                $.ajax({
                    type: 'GET',
                    url: this.getUrl() + '/used-by-as-substitute',
                    success: args.success,
                    error: args.error
                });
            },

            addTags: function (tags) {

                return $.ajax({
                    context: this,
                    type: 'POST',
                    url: this.url() + '/tags',
                    data: JSON.stringify({tags:tags}),
                    contentType: 'application/json; charset=utf-8',
                    success: function () {
                        this.fetch();
                    }
                });

            },

            removeTag: function (tag, callback, onError) {
                $.ajax({
                    type: 'DELETE',
                    url: this.url() + '/tags/' + tag,
                    success: function () {
                        callback();
                    },
                    error: onError
                });
            },

            removeTags: function (tags, callback, onError) {
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
                        },
                        error: onError
                    });
                });

            },


            checkout: function () {
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: this.url() + '/checkout',
                    success:function(){
                        this.fetch();
                        Backbone.Events.trigger('part:iterationChange');
                    },
                    error: function (xhr) {
                        window.alert(xhr.responseText);
                    }
                });
            },

            undocheckout: function () {
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: this.url() + '/undocheckout',
                    success:function(){
                        this.fetch();
                        Backbone.Events.trigger('part:iterationChange');
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
                    error: function (xhr) {
                        window.alert(xhr.responseText);
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
                    '/parts/#' +
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

            createNewVersion: function (description, workflow, roleMappingList, aclList, onSuccess, onError) {

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
                        this.collection.fetch({reset: true, success: onSuccess});
                    },
                    error: onError
                });
            },
            release: function () {
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: this.url() + '/release',
                    success: function () {
                        this.fetch();
                    }
                });
            },
            markAsObsolete: function () {
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: this.url() + '/obsolete',
                    success: function () {
                        this.fetch();
                    }
                });
            },

            getVisualizationUrl:function(){
                return App.config.contextPath + '/visualization/#assembly/' + App.config.workspaceId + '/' +  this.getPartKey() +'/0/0/0';
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
