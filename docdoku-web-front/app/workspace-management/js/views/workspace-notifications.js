/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-notifications.html',
    'views/notification-edit',
    'common-objects/models/workspace',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, NotificationEditView, Workspace, AlertView) {
    'use strict';

    var WorkspaceNotificationsView = Backbone.View.extend({

        events: {
            'click .toggle-checkbox':'toggleCheckbox'
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
                    return groupMemberships;
                })
                .then(Workspace.getUsersInGroups)
                .then(function(){
                    _.each(_this.users,function(user){
                        user.membership = _.select(_this.memberships,function(membership){
                            return membership.member.login === user.login;
                        }).map(function(membership){
                            return { login : membership.member.login, readOnly:membership.readOnly};
                        })[0] || null;
                        user.isCurrentAdmin = App.config.login === user.login;
                    });
                    return App.config.workspaceId;
                })
                .then(Workspace.getTags)
                .then(function(tags) {
                    _this.tags = tags;
                })
                .then(function(){
                    _this.$el.html(Mustache.render(template, {
                        i18n: App.config.i18n,
                        memberships:_this.memberships,
                        usersToManage:_this.users,
                        groupsToManage:_this.groupMemberships,
                        tags:_this.tags
                    }));
                    _this.bindDOMElements();
                });

            return this;
        },

        bindDOMElements:function(){
            this.$addTagForm = this.$('#workspace-add-tag-form');
            this.$addTagFormButton = this.$('.add-tag');
            this.$notifications = this.$('.notifications');
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        toggleButtons:function(){
            var nbOfSelection = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size();

            if (nbOfSelection > 0) {
                this.$addTagFormButton.toggle(true);

                if (nbOfSelection == 1) {
                    var groupSelected = this.$('#workspace_group_table tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');

                    if (groupSelected.size() == 1) {
                        var _this = this;
                        groupSelected.each(function(index, checkbox) {
                            _this.groupNotificationsEditView = new NotificationEditView({
                                id: checkbox.dataset.name,
                                type: 'group'
                            }).render();

                            _this.$('#user-group-subscriptions').append(_this.groupNotificationsEditView.el);
                        });
                    }

                    var userSelected = this.$('#workspace_user_table tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');

                    if (userSelected.size() == 1) {
                        var _this = this;
                        userSelected.each(function(index, checkbox) {
                            _this.userNotificationsEditView = new NotificationEditView({
                                id: checkbox.dataset.login,
                                type: 'user'
                            }).render();

                            _this.$('#user-subscriptions').append(_this.userNotificationsEditView.el);
                        });
                    }

                } else {
                    if (this.groupNotificationsEditView) {
                        this.groupNotificationsEditView.remove();
                    }
                    if (this.userNotificationsEditView) {
                        this.userNotificationsEditView.remove();
                    }
                }

            } else {
                this.$addTagFormButton.toggle(false);

                if (this.groupNotificationsEditView) {
                    this.groupNotificationsEditView.remove();
                }
                if (this.userNotificationsEditView) {
                    this.userNotificationsEditView.remove();
                }
            }
        },

        toggleCheckbox:function(e){
            this.toggleButtons();
        }

    });

    return WorkspaceNotificationsView;
});
