/*global define*/
define([
    'backbone',
    "mustache",
    "common-objects/collections/security/workspace_user_memberships",
    "common-objects/collections/security/workspace_user_group_memberships",
    "common-objects/views/security/membership_item",
    "common-objects/models/security/admin",
    "text!common-objects/templates/security/acl_entries.html"
], function (Backbone, Mustache, WorkspaceUserMemberships, WorkspaceUserGroupMemberships, MembershipItemView, Admin, template) {
    var ACLView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
            this.useACL = false;
        },

        render: function () {

            this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}));
            this.bindDomElements();

            var that = this;

            this.admin = new Admin();
            this.userMemberships = new WorkspaceUserMemberships();
            this.userGroupMemberships = new WorkspaceUserGroupMemberships();

            this.$aclSwitch.bootstrapSwitch();
            this.$aclSwitch.bootstrapSwitch('setState', this.useACL);

            this.$aclSwitch.on('switch-change', function (e, data) {
                that.useACL = !that.useACL;
                that.$usingAcl.toggleClass("hide");
            });


            this.admin.fetch({reset: true, success: function () {

                that.listenToOnce(that.userMemberships, "reset", that.onUserMembershipsReset);
                that.listenToOnce(that.userGroupMemberships, "reset", that.onUserGroupMembershipsReset);

                that.userMemberships.fetch({reset: true});
                that.userGroupMemberships.fetch({reset: true});
            }});

            return this;
        },

        bindDomElements: function () {
            this.$usersAcls = this.$("#users-acl-entries");
            this.$userGroupsAcls = this.$("#groups-acl-entries");
            this.$usingAcl = this.$(".using-acl");
            this.$aclSwitch = this.$(".acl-switch");
        },

        onUserMembershipsReset: function () {
            var that = this;
            this.userMemberships.each(function (userMembership) {
                var view = new MembershipItemView({model: userMembership, editMode: that.options.editMode && userMembership.getUserLogin() != that.admin.getLogin() && userMembership.getUserLogin() != APP_CONFIG.login}).render();
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

                var dto = {};
                dto.userEntries = {};
                dto.groupEntries = {};

                dto.userEntries.entry = [];
                dto.groupEntries.entry = [];
                this.userMemberships.each(function (userMembership) {
                    dto.userEntries.entry.push({key: userMembership.key(), value: userMembership.getPermission()});
                });
                this.userGroupMemberships.each(function (userGroupMembership) {
                    dto.groupEntries.entry.push({key: userGroupMembership.key(), value: userGroupMembership.getPermission()});
                });
                return dto;

            }

            return null;

        }

    });

    return ACLView;
});
