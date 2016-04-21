/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/security/workspace_user_memberships',
    'common-objects/collections/security/workspace_user_group_memberships',
    'common-objects/views/security/membership_item',
    'common-objects/models/security/acl_user_entry',
    'common-objects/models/security/acl_user_group_entry',
    'common-objects/views/security/acl_item',
    'common-objects/models/security/admin',
    'text!common-objects/templates/security/acl_edit.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, WorkspaceUserMemberships, WorkspaceUserGroupMemberships, MembershipItemView, ACLUserEntry, ACLUserGroupEntry, ACLItemView, Admin, template,AlertView) {
    'use strict';
	var ACLEditView = Backbone.View.extend({

        events: {
            'hidden #acl_edit_modal': 'onRemove',
            'submit form': 'onSubmit'
        },

        initialize: function () {
            _.bindAll(this);
            this.useACL = false;
            this.acl = this.options.acl;
            this.aclUserEntries = [];
            this.aclUserGroupEntries = [];
        },

        setTitle: function (title) {
            this.title = title;
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        bindDomElements: function () {
            this.$modal = this.$('#acl_edit_modal');
            this.$usersAcls = this.$('#users-acl-entries');
            this.$userGroupsAcls = this.$('#groups-acl-entries');
            this.$usingAcl = this.$('.using-acl');
            this.$aclSwitch = this.$('.acl-switch');
            this.$notifications = this.$('.notifications');
        },

        render: function () {

            var that = this;

            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, title: this.title}));

            this.bindDomElements();

            this.admin = new Admin();
            this.admin.fetch({reset: true, success: function () {

                that.useACL = false;

                if (that.acl) {
                    if (that.acl.userEntries.entry.length > 0 || that.acl.groupEntries.entry.length > 0) {
                        that.useACL = true;
                    }
                }

                if (!that.useACL) {
                    that.$usingAcl.addClass('hide');
                }

                if (!that.acl) {
                    that.onNoAclGiven();
                } else {

                    _.each(that.acl.userEntries.entry, function (entry) {
                        var userLogin = entry.key;
                        var permission = entry.value;
                        var editMode = that.options.editMode && userLogin !== that.admin.getLogin() && userLogin !== App.config.login;
                        var userAclView = new ACLItemView({model: new ACLUserEntry({userLogin: userLogin, permission: permission}), editMode: editMode}).render();
                        that.$usersAcls.append(userAclView.$el);
                        that.aclUserEntries.push(userAclView.model);
                    });


                    _.each(that.acl.groupEntries.entry, function (entry) {
                        var groupId = entry.key;
                        var permission = entry.value;
                        var editMode = that.options.editMode;
                        var groupAclView = new ACLItemView({model: new ACLUserGroupEntry({groupId: groupId, permission: permission}), editMode: editMode}).render();
                        that.$userGroupsAcls.append(groupAclView.$el);
                        that.aclUserGroupEntries.push(groupAclView.model);
                    });

                }

                that.$aclSwitch.bootstrapSwitch();
                that.$aclSwitch.bootstrapSwitch('setState', that.useACL);
                that.$aclSwitch.on('switch-change', function () {
                    that.useACL = !that.useACL;
                    that.$usingAcl.toggleClass('hide');
                });

            }});

            return this;
        },


        onNoAclGiven: function () {
            this.loadWorkspaceMembership();
        },

        loadWorkspaceMembership: function () {
            this.userMemberships = new WorkspaceUserMemberships();
            this.userGroupMemberships = new WorkspaceUserGroupMemberships();
            this.listenToOnce(this.userMemberships, 'reset', this.onUserMembershipsReset);
            this.listenToOnce(this.userGroupMemberships, 'reset', this.onUserGroupMembershipsReset);
            this.userMemberships.fetch({reset: true});
            this.userGroupMemberships.fetch({reset: true});
        },

        onUserMembershipsReset: function () {
            var that = this;
            this.userMemberships.each(function (userMembership) {
                var view = new ACLItemView({model: new ACLUserEntry({userLogin: userMembership.key(), permission: userMembership.getPermission()}), editMode: that.options.editMode && userMembership.key() !== that.admin.getLogin() && userMembership.key() !== App.config.login}).render();
                that.$usersAcls.append(view.$el);
                that.aclUserEntries.push(view.model);
            });
        },

        onUserGroupMembershipsReset: function () {
            var that = this;
            this.userGroupMemberships.each(function (userGroupMembership) {
                var view = new ACLItemView({model: new ACLUserGroupEntry({groupId: userGroupMembership.key(), permission: userGroupMembership.getPermission()}), editMode: that.options.editMode}).render();
                that.$userGroupsAcls.append(view.$el);
                that.aclUserGroupEntries.push(view.model);
            });
        },

        toList: function () {

            var dto = {};
            dto.userEntries = {};
            dto.groupEntries = {};

            dto.userEntries.entry = [];
            dto.groupEntries.entry = [];

            if (this.useACL) {
                _(this.aclUserEntries).each(function (aclEntry) {
                    dto.userEntries.entry.push({key: aclEntry.key(), value: aclEntry.getPermission()});
                });
                _(this.aclUserGroupEntries).each(function (aclEntry) {
                    dto.groupEntries.entry.push({key: aclEntry.key(), value: aclEntry.getPermission()});
                });
                return dto;
            }

            return null;

        },

        onSubmit: function (e) {
            this.trigger('acl:update');
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onError:function(error){
            var errorMessage = error ? error.responseText : error;
            var alertView = new AlertView({type: 'error',message: errorMessage}).render();
            this.$notifications.append(alertView.$el);
        },

        onRemove: function () {
            this.remove();
        }


    });

    return ACLEditView;
});
