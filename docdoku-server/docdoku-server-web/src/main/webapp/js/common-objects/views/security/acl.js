define([
    "common-objects/collections/security/workspace_user_memberships",
    "common-objects/collections/security/workspace_user_group_memberships",
    "common-objects/views/security/membership_item",
    "common-objects/models/security/admin",
    "text!common-objects/templates/security/acl_entries.html",
    "i18n!localization/nls/security-strings"
], function (
    WorkspaceUserMemberships,
    WorkspaceUserGroupMemberships,
    MembershipItemView,
    Admin,
    template,
    i18n
) {
    var ACLView = Backbone.View.extend({

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
        },

        render:function(){

            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();

            var that = this;

            this.admin = new Admin();
            this.userMemberships = new WorkspaceUserMemberships();
            this.userGroupMemberships = new WorkspaceUserGroupMemberships();

            this.admin.fetch({reset:true,success:function(){

                that.listenToOnce(that.userMemberships,"reset",that.onUserMembershipsReset);
                that.listenToOnce(that.userGroupMemberships,"reset",that.onUserGroupMembershipsReset);

                that.userMemberships.fetch({reset:true});
                that.userGroupMemberships.fetch({reset:true});
            }});

            return this;
        },

        bindDomElements:function(){
            this.$usersAcls = this.$("#users-acl-entries");
            this.$userGroupsAcls = this.$("#groups-acl-entries");
        }, 

        onUserMembershipsReset:function(){
            var that = this ;
            this.userMemberships.each(function(userMembership){
                var view = new MembershipItemView({model:userMembership, editMode:that.options.editMode && userMembership.getUserLogin() != that.admin.getLogin()  && userMembership.getUserLogin() != APP_CONFIG.login}).render();
                that.$usersAcls.append(view.$el);
            });
        },

        onUserGroupMembershipsReset:function(){
            var that = this ;
            this.userGroupMemberships.each(function(userGroupMembership){
                var view = new MembershipItemView({model:userGroupMembership, editMode:that.options.editMode}).render();
                that.$userGroupsAcls.append(view.$el);
            });
        },

        toList:function(){
            var dto = {};
            dto.userEntries = {};
            dto.groupEntries = {};
            this.userMemberships.each(function(userMembership){
                dto.userEntries[userMembership.key()]=userMembership.getPermission();
            });
            this.userGroupMemberships.each(function(userGroupMembership){
                dto.groupEntries[userGroupMembership.key()]=userGroupMembership.getPermission();
            });
            return dto;
        }
    });

    return ACLView;
});
