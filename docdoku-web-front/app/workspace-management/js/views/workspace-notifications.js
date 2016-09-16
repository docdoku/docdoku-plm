/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-notifications.html',
    'views/notification-edit',
    'common-objects/models/workspace',
    'common-objects/models/user_group',
    'common-objects/models/user',
    'common-objects/models/tag_subscription',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, NotificationEditView, Workspace, UserGroupModel, UserModel, TagSubscription, AlertView) {
    'use strict';

    var WorkspaceNotificationsView = Backbone.View.extend({

        events: {
            'click .toggle-checkbox':'toggleCheckbox',
            'click .add-tag':'addTagForm',
            'submit #workspace-add-tag-form':'onAddTagFormSubmit',
            'click #workspace-add-tag-form .cancel':'cancelAddTagForm'
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
            this.$userGroupSubscriptionViews = this.$('#user-group-subscriptions');
            this.$userSubscriptionViews = this.$('#user-subscriptions');
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        updateViews:function(){
            this.cancelAddTagForm();
            this.removeNotificationsEditViews();

            var nbOfSelection = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size();

            if (nbOfSelection > 0) {
                this.$addTagFormButton.toggle(true);

                if (nbOfSelection == 1) {
                    var groupSelected = this.$('#workspace_group_table tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');
                    var userSelected = this.$('#workspace_user_table tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');

                    if (groupSelected.size() == 1) {
                        this.groupNotificationsEditView = new NotificationEditView({
                            id: groupSelected[0].dataset.name,
                            type: 'group'
                        }).render();

                        this.$userGroupSubscriptionViews.append(this.groupNotificationsEditView.el);

                    } else if (userSelected.size() == 1) {
                        this.userNotificationsEditView = new NotificationEditView({
                            id: userSelected[0].dataset.login,
                            type: 'user'
                        }).render();

                        this.$userSubscriptionViews.append(this.userNotificationsEditView.el);
                    }
                }

            } else {
                this.$addTagFormButton.toggle(false);
            }
        },

        removeNotificationsEditViews: function () {
            if (this.groupNotificationsEditView) {
                this.groupNotificationsEditView.remove();
            }
            if (this.userNotificationsEditView) {
                this.userNotificationsEditView.remove();
            }
        },

        toggleCheckbox:function(e){
            this.updateViews();
        },

        addTagForm:function(){
            this.$addTagForm.removeClass('hide');
            this.$addTagFormButton.hide();
        },

        onAddTagFormSubmit:function(e){
            var _this = this;

            var newTagSubscription = new TagSubscription();
            newTagSubscription.setTag(this.$('#workspace-add-tag-form select#tag').val());
            newTagSubscription.setOnStateChange(this.$('#workspace-add-tag-form input[name="state-change"]')[0].checked);
            newTagSubscription.setOnIterationChange(this.$('#workspace-add-tag-form input[name="iteration-change"]')[0].checked);

            var groupsSelected = this.$('#workspace_group_table tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');
            var usersSelected = this.$('#workspace_user_table tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');
            var promises = [];

            groupsSelected.each(function(index, checkbox) {
                promises.push(UserGroupModel.addOrEditTagSubscription(App.config.workspaceId, checkbox.dataset.name, newTagSubscription, _this.onError.bind(_this)));
            });

            usersSelected.each(function(index, checkbox) {
                promises.push(UserModel.addOrEditTagSubscription(App.config.workspaceId, checkbox.dataset.login, newTagSubscription, _this.onError.bind(_this)));
            });

            $.when.apply($, promises).then(_this.updateViews.bind(_this));

            e.preventDefault();
            return false;
        },

        cancelAddTagForm:function(){
            this.$addTagForm.addClass('hide');
            this.$addTagFormButton.show();
        }

    });

    return WorkspaceNotificationsView;
});
