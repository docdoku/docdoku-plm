/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-notifications.html',
    'common-objects/models/workspace',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Workspace, AlertView) {
    'use strict';

    var WorkspaceNotificationsView = Backbone.View.extend({

        events: {
            'click .read-only':'readOnly'
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
            this.$notifications = this.$('.notifications');
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        }

    });

    return WorkspaceNotificationsView;
});
