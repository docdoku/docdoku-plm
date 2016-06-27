/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-users.html',
    'common-objects/models/workspace',
    'common-objects/views/alert'
], function (Backbone, Mustache,template, Workspace, AlertView) {
    'use strict';

    var WorkspaceUsersView = Backbone.View.extend({

        events: {
            'click .read-only':'readOnly',
            'change .toggle-checkboxes':'toggleCheckboxes',
            'click .toggle-checkbox':'toggleCheckbox',
            'change .toggle-checkbox':'toggleCheckboxChange',
            'click .delete-users':'deleteUsers',
            'click .delete-group':'deleteGroup',
            'click .add-user':'addUserForm',
            'click .add-group':'addGroupForm',
            'click .move-users':'moveUsers',
            'click .enable-users':'enableUsers',
            'click .enable-user':'enableUser',
            'click .disable-users':'disableUsers',
            'submit #workspace-add-user-form':'onAddUserFormSubmit',
            'submit #workspace-add-group-form':'onAddGroupFormSubmit',
            'click #workspace-add-user-form .cancel':'cancelAddUserForm',
            'click #workspace-add-group-form .cancel':'cancelAddGroupForm',
            'click .remove-users-from-group':'removeUsersFromGroup'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;
            _this.groupedUsers = [];
            Workspace.getUsers(App.config.workspaceId)
                .then(function(users) {
                    _this.users = users;
                    return App.config.workspaceId;
                })
                .then(Workspace.getUsersMemberships)
                .then(function(memberships) {
                    _this.memberships = memberships;
                    return App.config.workspaceId;
                })
                .then(Workspace.getUserGroupsMemberships)
                .then(function(groupMemberships){
                    _this.groupMemberships = groupMemberships;
                    return {
                        groups: groupMemberships,
                        next: function (group,users) {
                            group.users = users;
                            _.each(users, function (user) {
                                user.isCurrentAdmin = user.login === App.config.login;
                                _this.groupedUsers.push({user: user});
                            });
                            return users;
                        }
                    };
                })
                .then(Workspace.getUsersInGroups)
                .then(function(){
                    _.each(_this.users,function(user){
                        user.membership = _.select(_this.memberships,function(membership){
                                return membership.member.login === user.login;
                            }).map(function(membership){
                                return { login : membership.member.login, readOnly:membership.readOnly};
                            })[0] || null;
                        user.groupsMemberships = _.select(_this.groupedUsers,function(groupMembership){
                                return groupMembership.user.login === user.login;
                            }).map(function(groupMembership){
                                return { login : groupMembership.user.login, readOnly:groupMembership.readOnly};
                            })[0] || null;
                        user.isCurrentAdmin = App.config.login === user.login;
                    });
                })
                .then(function(){
                    _this.$el.html(Mustache.render(template, {
                        i18n: App.config.i18n,
                        memberships:_this.memberships,
                        usersToManage:_this.users,
                        groupMemberships: _this.groupMemberships
                    }));
                    _this.bindDOMElements();
                });

            return this;
        },

        bindDOMElements:function(){
            this.$addUserForm = this.$('#workspace-add-user-form');
            this.$addGroupForm = this.$('#workspace-add-group-form');
            this.$addUserFormButton = this.$('.add-user');
            this.$addGroupFormButton = this.$('.add-group');
            this.$notifications = this.$('.notifications');
            this.bindGroupSwitches();
            this.bindUserSwitches();
        },

        bindGroupSwitches:function(){
            var _this = this;
            this.$groupSwitch = this.$('.group-readonly-switch');
            this.$groupSwitch.bootstrapSwitch();
            this.$groupSwitch.on('switch-change', function (e,data) {
                var memberId = e.target.dataset.memberId;
                var fullAccess = data.value;
                Workspace.setGroupAccess(App.config.workspaceId,{memberId:memberId,readOnly:!fullAccess})
                    .then(function(){},_this.onError.bind(_this));
            });
        },

        bindUserSwitches:function(){
            var _this = this;
            this.$userSwitch = this.$('.user-readonly-switch');
            this.$userSwitch.bootstrapSwitch();
            this.$userSwitch.on('switch-change', function (e,data) {
                var login = e.target.dataset.login;
                var fullAccess = data.value;
                Workspace.setUsersMembership(App.config.workspaceId,{login:login,membership:fullAccess?'FULL_ACCESS':'READ_ONLY'})
                    .then(function(){},_this.onError.bind(_this));
            });
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        cancelAddUserForm:function(){
            this.$addUserForm.addClass('hide');
            this.$addUserFormButton.show();
        },

        cancelAddGroupForm:function(){
            this.$addGroupForm.addClass('hide');
            this.$addGroupFormButton.show();
        },

        addUserForm:function(){
            this.$addUserForm.removeClass('hide');
            this.$addUserFormButton.hide();
        },
        addGroupForm:function(){
            this.$addGroupForm.removeClass('hide');
            this.$addGroupFormButton.hide();
        },
        onAddUserFormSubmit:function(e){
            var login = this.$('#workspace-add-user-form input[name="login"]').val().trim();
            if(login){
                Workspace.addUser(App.config.workspaceId,{login:login})
                    .then(this.render.bind(this),this.onError.bind(this));
            }
            e.preventDefault();
            return false;
        },
        onAddGroupFormSubmit:function(e){
            var groupId = this.$('#workspace-add-group-form input[name="groupId"]').val().trim();
            if(groupId){
                Workspace.addGroup(App.config.workspaceId,{id:groupId})
                    .then(this.render.bind(this),this.onError.bind(this));
            }
            e.preventDefault();
            return false;
        },

        toggleButtons:function(){
            var hasUsers = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size() > 0;
            var hasGroupUsers = this.$('table.group_user_table > tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size() > 0;

            this.$('.show-if-users').toggle(hasUsers);
            this.$('.show-if-group-users').toggle(hasGroupUsers);
        },

        deleteUsers:function(){
            var userLogins=[];
            this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked')
                .each(function(index,checkbox){
                    userLogins.push(checkbox.dataset.login);
                });

            var usersToDelete = _.without(_.uniq(userLogins),App.config.login);

            Workspace.removeUsersFromWorkspace(App.config.workspaceId, usersToDelete)
                .then(this.render.bind(this), this.onError.bind(this));

        },

        moveUsers:function(e){
            var groupId = e.target.dataset.groupId;
            var userLogins=[];
            this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked')
                .each(function(index,checkbox){
                    userLogins.push(checkbox.dataset.login);
                });

            var usersToMove = _.uniq(userLogins);

            Workspace.moveUsers(App.config.workspaceId, groupId, usersToMove)
                .then(this.render.bind(this), this.onError.bind(this));

        },

        removeUsersFromGroup:function(){
            var promiseArray = [];
            this.$('table.group_user_table > tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked')
                .each(function(index,checkbox){
                    promiseArray.push(Workspace.removeUserFromGroup(App.config.workspaceId, checkbox.dataset.memberId, checkbox.dataset.login));
                });
            $.when.apply(undefined, promiseArray).then(this.render.bind(this), this.onError.bind(this));
        },

        enableUser:function(e){
            Workspace.enableUser(App.config.workspaceId, {login: e.target.dataset.login})
                .then(this.render.bind(this), this.onError.bind(this));
        },

        enableUsers:function(){
            var users=[];
            this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked')
                .each(function(index,checkbox){
                    users.push({login:checkbox.dataset.login});
                });
            Workspace.enableUsers(App.config.workspaceId, users)
                .then(this.render.bind(this), this.onError.bind(this));
        },

        disableUsers:function(){
            var users=[];
            this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked')
                .each(function(index,checkbox){
                    users.push({login:checkbox.dataset.login});
                });
            Workspace.disableUsers(App.config.workspaceId, users)
                .then(this.render.bind(this), this.onError.bind(this));

        },

        deleteGroup:function(e){
            Workspace.removeGroup(App.config.workspaceId, e.target.dataset.groupId)
                .then(this.render.bind(this), this.onError.bind(this));
        },

        toggleCheckboxChange:function(){
        },

        toggleCheckbox:function(e){
            console.log('click ' + e.target);
            this.toggleButtons();
        },

        toggleCheckboxes:function(e){
            var $table = $(e.target).parents('table');
            $table.find('tbody > tr > td:nth-child(1) > input[type="checkbox"]').prop('checked', e.target.checked).trigger('change');
            this.toggleButtons();
        }

    });

    return WorkspaceUsersView;
});
