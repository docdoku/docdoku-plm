/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/workflow/role_item.html'
],
function (Backbone, Mustache, template) {
    'use strict';
    var RoleItemView = Backbone.View.extend({
        className: 'well roles-item',

        events: {
            'click .fa-times': 'removeAndNotify',
            'change select.role-default-assigned-users': 'updateAssignedUsers',
            'change select.role-default-assigned-groups': 'updateAssignedGroups'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                model: this.model,
                required: !this.options.nullable
            }));
            this.$usersSelect = this.$('select.role-default-assigned-users');
            this.$groupsSelect = this.$('select.role-default-assigned-groups');
            this.fillUserList();
            this.fillGroupList();
            return this;
        },

        fillUserList: function () {

            this.$usersSelect.selectize({
                plugins: ['remove_button'],
                valueField: 'login',
                searchField: ['name'],
                render: {
                    item: function(item, escape) {
                        return '<div><span class="name">' + escape(item.name) + '</span></div>';
                    },
                    option: function(item, escape) {
                        return '<div><span class="label">' + escape(item.name) + '</span></div>';
                    }
                },
                options:this.options.userList.models.map(function(user){
                    return {login:user.getLogin(), name:user.getName()};
                })
            });

            _.each(this.model.getDefaultAssignedUsers(),function(user){
                this.$usersSelect[0].selectize.addItem(user.login);
            },this);
        },

        fillGroupList: function () {

            this.$groupsSelect.selectize({
                plugins: ['remove_button'],
                valueField: 'id',
                searchField: ['id'],
                render: {
                    item: function(item, escape) {
                        return '<div><span class="name">' + escape(item.id) + '</span></div>';
                    },
                    option: function(item, escape) {
                        return '<div><span class="label">' + escape(item.id) + '</span></div>';
                    }
                },
                options:this.options.groupList.models.map(function(group){
                    return {id:group.id};
                })
            });

            _.each(this.model.getDefaultAssignedGroups(),function(group){
                this.$groupsSelect[0].selectize.addItem(group.id);
            },this);

        },

        removeAndNotify: function () {
            if (this.options.removable) {
                this.remove();
                this.trigger('view:removed');
            } else {
                if(_.isFunction(this.options.onError)){
                    this.options.onError(App.config.i18n.ALERT_ROLE_IN_USE);
                } else {
                    window.alert(App.config.i18n.ALERT_ROLE_IN_USE);
                }
            }
        },

        updateAssignedUsers: function () {
            this.model.setDefaultAssignedUsers((this.$usersSelect.val()||[]).map(function(login){
                return {login:login};
            }));
        },

        updateAssignedGroups:function(){
            this.model.setDefaultAssignedGroups((this.$groupsSelect.val()||[]).map(function(id){
                return {id:id};
            }));
        }

    });

    return RoleItemView;

});
