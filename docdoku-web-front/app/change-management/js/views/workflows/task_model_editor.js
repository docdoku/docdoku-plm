/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/task_model',
    'common-objects/models/role',
    'text!templates/workflows/task_model_editor.html'
], function (Backbone, Mustache, TaskModel, Role, template) {
    'use strict';
    var TaskModelEditorView = Backbone.View.extend({

        tagName: 'li',

        className: 'task-section',

        events: {
            'click button.delete-task': 'deleteTaskAction',
            'click p.task-name': 'gotoUnfoldState',
            'click i.fa-minus': 'gotoFoldState',
            'click .new-role': 'newRoleAction',
            'click .add-role': 'addRoleAction',
            'change input.task-name': 'titleChanged',
            'change textarea.instructions': 'instructionsChanged',
            'change select.role': 'roleSelected'
        },

        States: {
            FOLD: 0,
            UNFOLD: 1
        },

        initialize: function () {
            this.isNewRole = false;
            this.roles = this.options.roles;
            this.roles.on('add', this.onRoleAdd, this);
            this.newRoles = this.options.newRoles;

            this.state = this.States.FOLD;
            if (_.isUndefined(this.model.get('role'))) {
                this.model.set({
                    role: this.options.roles.at(0)
                });
            }
        },
        render: function () {
            var _this = this;
            this.template = Mustache.render(template, {
                cid: this.model.cid,
                task: this.model.attributes,
                roles: this.roles.pluck('name'),
                i18n: App.config.i18n
            });
            this.$el.html(this.template);
            this.bindDomElements();

            // Select right role
            _.each(this.roles.models, function (role) {
                if (_this.model.get('role') && _this.model.get('role').get('name') === role.get('name')) {
                    _this.roleSelect.val(role.get('name'));
                }
            });
            return this;
        },
        bindDomElements: function () {
            this.divTask = this.$('div.task');
            this.taskContent = this.$('.task-content');
            this.pTitle = this.$('p.task-name');
            this.inputTitle = this.$('input.task-name');
            this.textareaInstructions = this.$('textarea.instructions');
            this.roleInput = this.$('.role-input');
            this.roleSelect = this.$('.role-select');
        },

        titleChanged: function () {
            this.model.set({
                title: this.inputTitle.val()
            });
            if (this.inputTitle.val().length === 0) {
                this.pTitle.html(App.config.i18n.TASK_NAME_PLACEHOLDER);
            }
            else {
                this.pTitle.html(this.inputTitle.val());
            }
        },
        instructionsChanged: function () {
            this.model.set({
                instructions: this.textareaInstructions.val()
            });
        },
        onRoleAdd: function (model) {
            this.roleSelect.append('<option value="' + model.getName() + '">' + model.getName() + '</option>');
        },


        deleteTaskAction: function () {
            this.model.collection.remove(this.model);
            this.unbindAllEvents();
            this.remove();
        },

        newRoleAction: function () {
            this.roleInput.val('');
            this.taskContent.addClass('new-role');
            return false;
        },
        addRoleAction: function () {
            var roleName = this.roleInput.val();
            if (roleName) {
                var selectedRole = this.roles.findWhere({name: roleName});
                if (!selectedRole) {
                    selectedRole = new Role({
                        workspaceId: App.config.workspaceId,
                        name: roleName,
                        defaultAssignedUsers: [],
                        defaultAssignedGroups: []
                    });
                    this.newRoles.push(selectedRole);
                    this.roles.add(selectedRole);
                }
                this.roleSelect.val(roleName);
                this.model.set({
                    role: selectedRole
                });
            }
            this.taskContent.removeClass('new-role');
            return false;
        },
        roleSelected: function (e) {
            var nameSelected = e.target.value;
            var roleSelected = _.find(this.roles.models, function (role) {
                return nameSelected === role.get('name');
            });
            this.model.set({
                role: roleSelected
            });
        },

        gotoFoldState: function () {
            this.state = this.States.FOLD;
            this.divTask.removeClass('unfold');
            this.divTask.addClass('fold');
            this.inputTitle.prop('readonly', true);
        },
        gotoUnfoldState: function () {
            this.state = this.States.UNFOLD;
            this.divTask.removeClass('fold');
            this.divTask.addClass('unfold');
            this.inputTitle.prop('readonly', false);
        },

        unbindAllEvents: function () {
            this.undelegateEvents();
        }

    });
    return TaskModelEditorView;
});
