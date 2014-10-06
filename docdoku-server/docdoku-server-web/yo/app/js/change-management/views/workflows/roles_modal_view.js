/*global _,define,App*/
define([
	'mustache',
	'common-objects/views/components/modal',
	'text!templates/workflows/roles_modal.html',
	'common-objects/models/role',
	'common-objects/collections/roles',
	'common-objects/collections/roles_in_use',
	'common-objects/views/workflow/role_item_view',
	'common-objects/collections/users'
],function (Mustache, ModalView, template, Role, RolesList, RoleInUseList, RoleItemView, UserList) {
	'use strict';
    var RolesModalView = ModalView.extend({
        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['submit #form-new-role'] = 'onSubmitNewRole';
            this.events['submit #form-roles'] = 'onSubmitForm';
        },

        render: function () {
            var self = this;

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));

            this.$roleViews = this.$('#form-roles');
            this.$newRoleName = this.$('input.role-name');


            this.userList = new UserList();
            this.collection = new RolesList();
            this.rolesInUse = new RoleInUseList();

            this.rolesToDelete = [];

            this.listenTo(this.collection, 'reset', this.onCollectionReset);
            this.listenTo(this.collection, 'add', this.onModelAddedToCollection);

            this.userList.fetch({reset: true, success: function () {
                self.rolesInUse.fetch({reset: true, success: function () {
                    self.collection.fetch({reset: true});
                }});
            }});

            return this;
        },

        onSubmitNewRole: function (e) {
            this.collection.create({workspaceId: App.config.workspaceId, name: this.$newRoleName.val(), defaultUserMapped: null});
            this.resetNewRoleForm();

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onSubmitForm: function (e) {
            var self = this;

            // Update models which has changed
            this.collection.each(function (model) {
                if (model.hasChanged('defaultUserMapped') && !_.contains(self.rolesToDelete, model)) {
                    model.save();
                }
            });

            // Delete roles marked for delete
            _.each(this.rolesToDelete, function (model) {
                model.destroy();
            });

            this.hide();

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onCollectionReset: function () {
            var self = this;
            this.roleViews = [];
            this.collection.each(function (model) {
                self.addRoleView(model);
            });
        },

        resetNewRoleForm: function () {
            this.$newRoleName.val('');
        },

        onModelAddedToCollection: function (model) {
            this.addRoleView(model);
        },

        addRoleView: function (model) {
            var self = this;
            var modelCanBeRemoved = this.checkRemovable(model);
            var view = new RoleItemView({model: model, userList: this.userList, nullable: true, removable: modelCanBeRemoved}).render();
            this.roleViews.push(view);
            this.$roleViews.append(view.$el);
            view.on('view:removed', function () {
                self.rolesToDelete.push(model);
            });
        },

        checkRemovable: function (pModel) {
            var removable = true;
            this.rolesInUse.each(function (model) {
                if (pModel.getName() === model.getName()) {
                    removable = false;
                }
            });
            return removable;
        }
    });
    return RolesModalView;
});