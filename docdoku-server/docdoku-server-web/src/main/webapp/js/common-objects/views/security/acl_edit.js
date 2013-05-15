 define([
    "common-objects/collections/security/workspace_user_memberships",
    "common-objects/collections/security/workspace_user_group_memberships",
    "common-objects/views/security/membership_item",
    "common-objects/models/security/acl_user_entry",
    "common-objects/models/security/acl_user_group_entry",
    "common-objects/views/security/acl_item",
    "text!common-objects/templates/security/acl_edit.html",
    "i18n!localization/nls/security-strings"
], function (
    WorkspaceUserMemberships,
    WorkspaceUserGroupMemberships,
    MembershipItemView,
    ACLUserEntry,
    ACLUserGroupEntry,
    ACLItemView,
    template,
    i18n
) {
    var ACLEditView = Backbone.View.extend({

        events:{
            "hidden #acl_edit_modal":"destroy",
            "submit form":"onSubmit"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
            this.acl = this.options.acl;
            this.aclUserEntries = [];
            this.aclUserGroupEntries = [];
        },

        setTitle:function(title){
            this.title = title;
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        bindDomElements:function(){
            this.$modal = this.$('#acl_edit_modal');
            this.$usersAcls = this.$("#users-acl-entries");
            this.$userGroupsAcls = this.$("#groups-acl-entries");
        },

        render:function(){

            this.$el.html(this.template({i18n:i18n, title:this.title}));

            this.bindDomElements();

            if(this.acl == null){
                this.onNoAclGiven();
            }else{
                var that = this ;

                for(var key in this.acl.userEntries){
                    var view = new ACLItemView({model:new ACLUserEntry({userLogin : key, permission :this.acl.userEntries[key]}), editMode:that.options.editMode && key != APP_CONFIG.login}).render();
                    that.$usersAcls.append(view.$el);
                    this.aclUserEntries.push(view.model);
                }

                for(var key in this.acl.groupEntries){
                    var view = new ACLItemView({model:new ACLUserGroupEntry({groupId : key, permission :this.acl.groupEntries[key]}), editMode:that.options.editMode}).render();
                    that.$userGroupsAcls.append(view.$el);
                    this.aclUserGroupEntries.push(view.model);
                }
            }

            return this;
        },


        onNoAclGiven:function(){
            this.loadWorkspaceMembership();
        },

        loadWorkspaceMembership:function(){
            this.userMemberships = new WorkspaceUserMemberships();
            this.userGroupMemberships = new WorkspaceUserGroupMemberships();
            this.listenToOnce(this.userMemberships,"reset",this.onUserMembershipsReset);
            this.listenToOnce(this.userGroupMemberships,"reset",this.onUserGroupMembershipsReset);
            this.userMemberships.fetch({reset:true});
            this.userGroupMemberships.fetch({reset:true});
        },

        onUserMembershipsReset:function(){
            var that = this ;
            this.userMemberships.each(function(userMembership){
                var view = new ACLItemView({model:new ACLUserEntry({userLogin : userMembership.key(), permission :userMembership.getPermission()}), editMode:that.options.editMode && userMembership.key() != APP_CONFIG.login}).render();
                that.$usersAcls.append(view.$el);
                that.aclUserEntries.push(view.model);
            });
        },

        onUserGroupMembershipsReset:function(){
            var that = this ;
            this.userGroupMemberships.each(function(userGroupMembership){
                var view = new ACLItemView({model:new ACLUserGroupEntry({groupId : userGroupMembership.key(), permission :userGroupMembership.getPermission()}), editMode:that.options.editMode}).render();
                that.$userGroupsAcls.append(view.$el);
                that.aclUserGroupEntries.push(view.model);
            });
        },


        toList:function(){
            var dto = {};
            dto.userEntries = {};
            dto.groupEntries = {};
            _(this.aclUserEntries).each(function(aclEntry){
                dto.userEntries[aclEntry.key()]=aclEntry.getPermission();
            });
            _(this.aclUserGroupEntries).each(function(aclEntry){
                dto.groupEntries[aclEntry.key()]=aclEntry.getPermission();
            });
            return dto;
        },

        onSubmit:function(e){
            var that = this;
            this.trigger("acl:update",this.toList());
            e.preventDefault();
            e.stopPropagation();
            return false ;
        }
    });

    return ACLEditView;
});
