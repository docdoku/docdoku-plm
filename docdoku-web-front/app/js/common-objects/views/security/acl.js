/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/security/workspace_user_memberships',
    'common-objects/collections/security/workspace_user_group_memberships',
    'common-objects/views/security/membership_item',
    'common-objects/models/security/admin',
    'text!common-objects/templates/security/acl_entries.html'
], function (Backbone, Mustache, WorkspaceUserMemberships, WorkspaceUserGroupMemberships, MembershipItemView, Admin, template) {
	'use strict';
    var ACLView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
            this.useACL = false;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();

            var that = this;

            this.admin = new Admin();
            this.userMemberships = new WorkspaceUserMemberships();
            this.userGroupMemberships = new WorkspaceUserGroupMemberships();

            this.$aclSwitch.bootstrapSwitch();
            this.$aclSwitch.bootstrapSwitch('setState', this.useACL);

            this.$aclSwitch.on('switch-change', function () {
                that.useACL = !that.useACL;
                that.$usingAcl.toggleClass('hide');
            });


            this.admin.fetch({reset: true, success: function () {

                that.listenToOnce(that.userMemberships, 'reset', that.onUserMembershipsReset);
                that.listenToOnce(that.userGroupMemberships, 'reset', that.onUserGroupMembershipsReset);

                that.userMemberships.fetch({reset: true});
                that.userGroupMemberships.fetch({reset: true});
            }});

            return this;
        },

        bindDomElements: function () {
            this.$usersAcls = this.$('#users-acl-entries');
            this.$userGroupsAcls = this.$('#groups-acl-entries');
            this.$usingAcl = this.$('.using-acl');
            this.$aclSwitch = this.$('.acl-switch');
        },

        onUserMembershipsReset: function () {
            var that = this;
            this.userMemberships.each(function (userMembership) {
                var view = new MembershipItemView({model: userMembership, editMode: that.options.editMode && userMembership.getUserLogin() !== that.admin.getLogin() && userMembership.getUserLogin() !== App.config.login}).render();
                that.$usersAcls.append(view.$el);
            });
        },

        onUserGroupMembershipsReset: function () {
            var that = this;
            this.userGroupMemberships.each(function (userGroupMembership) {
                var view = new MembershipItemView({model: userGroupMembership, editMode: that.options.editMode}).render();
                that.$userGroupsAcls.append(view.$el);
            });
        },

        toList: function () {

            if (this.useACL) {

                var data = {};
                data.userEntries = [];
                data.groupEntries = [];

                this.userMemberships.each(function (userMembership) {
                    data.userEntries.push({key: userMembership.key(), value: userMembership.getPermission()});
                });
                this.userGroupMemberships.each(function (userGroupMembership) {
                    data.groupEntries.push({key: userGroupMembership.key(), value: userGroupMembership.getPermission()});
                });
                return data;

            }

            return null;

        }

    });

    return ACLView;
});
