/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-users.html',
    'common-objects/models/workspace'
], function (Backbone, Mustache, template, Workspace) {
    'use strict';

    var WorkspaceUsersView = Backbone.View.extend({

        events: {
            'click .read-only':'readOnly',
            'change .toggle-checkboxes':'toggleCheckboxes',
            'click .toggle-checkbox':'toggleCheckbox',
            'change .toggle-checkbox':'toggleCheckboxChange',
            'click .delete-users':'deleteUsers'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;
            Workspace.getUsersMemberships(App.config.workspaceId)
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
                    _.each(_this.memberships,function(membership){
                        membership.isCurrentAdmin = membership.member.login === App.config.login;
                    });
                    _this.$el.html(Mustache.render(template, {
                        i18n: App.config.i18n,
                        memberships:_this.memberships,
                        groupMemberships: _this.groupMemberships
                    }));
                });

            return this;
        },

        toggleButtons:function(){
            var hasUsers = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size() > 0;
            var hasGroupUsers = this.$('table.group_user_table > tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size() > 0;

            this.$('.show-if-users').toggle(hasUsers);
            this.$('.show-if-group-users').toggle(hasGroupUsers);
        },

        deleteUsers:function(){
            var hasUsers = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');
        },

        toggleCheckboxChange:function(e){
            console.log('change ' + e.target)
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
