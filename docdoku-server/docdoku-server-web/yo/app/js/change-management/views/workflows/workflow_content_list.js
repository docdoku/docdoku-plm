/*global define,bootbox,App*/
define([
    'require',
    'backbone',
    'mustache',
    'collections/roles',
    'views/workflows/workflow_list',
    'views/workflows/roles_modal_view',
    'common-objects/views/security/acl_edit',
    'text!templates/workflows/workflow_content_list.html',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'common-objects/views/alert'
], function (require, Backbone, Mustache, RoleList, WorkflowListView, RolesModalView,ACLEditView, template, deleteButton,aclButton, AlertView) {
	'use strict';
    var WorkflowContentListView = Backbone.View.extend({

        events:{
            'click .actions .new':'actionNew',
            'click .actions .delete':'actionDelete',
            'click .actions .roles': 'actionRoles',
            'click .actions .edit-acl': 'onEditAcl'
        },

        partials: {
            deleteButton: deleteButton,
            aclButton:  aclButton
        },

        initialize: function () {
            this.rolesList = new RoleList();
        },

        render:function(){
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId},this.partials));
            this.bindDomElement();
            this.listView = new WorkflowListView({
                el: this.$('.workflow-table')
            });
            this.listView.on('selectionChange', this.selectionChanged.bind(this));
            this.bindCollections();
        },
        destroyed: function () {
            this.$el.html('');
        },

        bindDomElement : function(){
            this.$notifications = this.$('.notifications');
            this.$newWorflowBtn = this.$('.actions .new');
            this.$aclButton = this.$('.actions .edit-acl');
            this.$deleteButton = this.$('.actions .delete');

        },
        bindCollections : function(){
            this.listenTo(this.listView.collection, 'reset', this.onWorkflowsListChange);
            this.listView.collection.fetch({reset: true});
            this.listenTo(this.rolesList, 'reset', this.onRolesListChange);
            this.rolesList.fetch({reset: true});
            this.listenTo(this.listView.collection, 'change', this.onWorkflowsListChange);
        },

        selectionChanged: function () {

            var checkedViews = this.listView.checkedViews();
            switch (checkedViews.length) {
                case 0:
                    this.onNoWorkflowSelected();
                    break;
                case 1:
                    this.onOneWorkflowSelected(checkedViews[0].model);
                    break;
                default:
                    this.onSeveralWorkflowSelected();
                    break;
            }
        },

        actionNew: function () {
            this.$notifications.html('');
            App.router.navigate(App.config.workspaceId + '/workflow-model-editor', {trigger: true});
            return false;
        },
        actionDelete: function () {
            var _this = this;

            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_WORKFLOW, function(result) {
                if (result) {

                    _this.listView.eachChecked(function (view) {
                        view.model.destroy({
                            wait:true,
                            dataType: 'text',
                            success: function () {
                                _this.listView.redraw();
                                _this.onNoWorkflowSelected();
                            },
                            error:_this.onError.bind(_this)
                        });
                    });
                }
            });
            return false;
        },
        actionRoles: function () {
            this.$notifications.html('');
            new RolesModalView({
                collection: this.rolesList
            }).show();
        },

        onRolesListChange: function(){
            var _this = this;
            var isRolesListEmpty = _this.rolesList.length === 0;
            if (isRolesListEmpty) {
                _this.$notifications.append(new AlertView({
                    type: 'warning',
                    message: App.config.i18n.WARNING_ANY_ROLE
                }).render().$el);

                _this.$newWorflowBtn.attr('disabled', 'disabled');
            } else {
                _this.$newWorflowBtn.removeAttr('disabled');
            }
        },
        onWorkflowsListChange: function(){
            var _this = this;
            var isWorkflowListEmpty = _this.listView.collection.length === 0;
            if (isWorkflowListEmpty) {
                _this.$notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.ADVICE_CREATE_WORKFLOW
                }).render().$el);
            }
        },

        onNoWorkflowSelected: function () {
            this.$aclButton.hide();
            this.$deleteButton.hide();
        },

        onOneWorkflowSelected: function () {
            this.$aclButton.show();
            this.$deleteButton.show();

        },
        onSeveralWorkflowSelected: function () {
            this.$aclButton.hide();
            this.$deleteButton.show();
        },
        onEditAcl: function () {

            var self = this;

            var workflowSelected;

            this.listView.eachChecked(function (view) {
                workflowSelected = view.model;
            });

            var aclEditView = new ACLEditView({
                editMode: true,
                acl: workflowSelected.get('acl')
            });

            aclEditView.setTitle(workflowSelected.getId());
            window.document.body.appendChild(aclEditView.render().el);

            aclEditView.openModal();
            aclEditView.on('acl:update', function () {

                var acl = aclEditView.toList();

                workflowSelected.updateWorkflowACL({
                    acl: acl || {userEntries: {}, groupEntries: {}},
                    success: function () {
                        workflowSelected.set('acl', acl);
                        aclEditView.closeModal();
                        self.listView.redraw();
                    },
                    error: function(model, error){
                        aclEditView.onError(model, error);
                    }
                });
            });

            return false;
        },

        onError: function(model, error) {
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        }

    });

    return WorkflowContentListView;
});
