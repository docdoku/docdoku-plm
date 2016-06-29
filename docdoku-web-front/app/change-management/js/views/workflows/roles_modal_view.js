/*global _,define,App*/
define([
	'mustache',
	'common-objects/views/components/modal',
	'text!templates/workflows/roles_modal.html',
	'common-objects/models/role',
	'collections/roles',
	'collections/roles_in_use',
	'common-objects/views/workflow/role_item_view',
    'common-objects/views/alert',
	'common-objects/collections/users',
	'common-objects/collections/user_groups'
],function (Mustache, ModalView, template, Role, RolesList, RoleInUseList, RoleItemView, AlertView, UserList, UserGroupList) {
	'use strict';
    var RolesModalView = ModalView.extend({
        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['submit #form-new-role'] = 'onSubmitNewRole';
            this.events['submit #form-roles'] = 'onSubmitForm';
        },

        render: function () {
            var _this = this;

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));
            this.bindDomElement();

            this.$roleViews = this.$('#form-roles');
            this.$newRoleName = this.$('input.role-name');

            this.$newRoleName.customValidity(App.config.i18n.REQUIRED_FIELD);

            this.userList = new UserList();
            this.groupList = new UserGroupList();

            if(!this.collection){
                this.collection = new RolesList();
            }

            this.rolesInUse = new RoleInUseList();

            this.groupList.fetch({reset: true, success: function () {
                _this.userList.fetch({reset: true, success: function () {
                    _this.createRoleViews();
                    _this.rolesInUse.fetch({reset: true});
                }});
            }});

            this.rolesToDelete = [];

            this.listenTo(this.collection, 'add', this.onModelAddedToCollection);

            return this;
        },

        bindDomElement: function(){
            this.$notifications = this.$el.find('.notifications').first();
        },

        onError:function(model, error){
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
            this.collection.fetch();
        },

        onSubmitNewRole: function (e) {
            if(this.$newRoleName.val().trim() && !this.collection.findWhere({name:this.$newRoleName.val()})){
                this.collection.add({
                    workspaceId: App.config.workspaceId,
                    name: this.$newRoleName.val(),
                    defaultAssignedUsers: [],
                    defaultAssignedGroups: []
                });
                this.resetNewRoleForm();
            }
            e.preventDefault();
            e.stopPropagation();
            return false;
        },
        onSubmitForm: function (e) {
            var _this = this;
            var toSave = this.collection.length;
            var toDelete = this.rolesToDelete.length;

            // Update models which has changed
            this.collection.each(function (model) {
                if(!_.contains(_this.rolesToDelete, model)){
                    model.save(null,{
                        success: function(){
                            toSave--;
                            if(!toDelete && !toSave){
                                _this.hide();
                            }
                        },
                        error: _this.onError
                    });
                }else{
                    toSave--;
                }
            });

            // Delete roles marked for delete
            _.each(this.rolesToDelete, function (model) {
                model.destroy({
                    dataType: 'text', // server doesn't send a json hash in the response body
                    success: function(){
                        toDelete--;
                        if(!toDelete && !toSave){
                            _this.hide();
                        }
                    },
                    error: _this.onError
                });
            });

            if(!toDelete && !toSave){
                _this.hide();
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        createRoleViews: function () {
            var _this = this;
            this.roleViews = [];
            this.collection.each(function (model) {
                _this.addRoleView(model);
            });
        },

        resetNewRoleForm: function () {
            this.$newRoleName.val('');
        },

        onModelAddedToCollection: function (model) {
            var _this = this;
            this.addRoleView(model, function(){
                _this.collection.remove(model);
            });
        },

        addRoleView: function (model,onRemove) {
            var _this = this;
            var onViewRemove = (_.isFunction(onRemove)) ? onRemove : function () {
                _this.rolesToDelete.push(model);
            };

            var modelCanBeRemoved = this.checkRemovable(model);
            var view = new RoleItemView({
                model: model,
                userList: this.userList,
                groupList: this.groupList,
                nullable: true,
                removable: modelCanBeRemoved,
                onError: _this.onError
            }).render();
            this.roleViews.push(view);
            this.$roleViews.append(view.$el);
            this.addSubView(view);

            view.on('view:removed', onViewRemove);
        },

        checkRemovable: function (pModel) {
            var removable = true;
            this.rolesInUse.each(function (model) {
                if (pModel.getName() === model.getName()) {
                    removable = false;
                }
            });
            return removable;
        },

        hidden: function () {
            this.collection.fetch({reset:true});
            this.destroy();
        }
    });
    return RolesModalView;
});
