/*global define,App*/
define([
    'require',
    'collections/roles',
    'views/workflows/workflow_list',
    'views/workflows/roles_modal_view',
    'text!templates/workflows/workflow_content_list.html',
    'text!common-objects/templates/buttons/delete_button.html',
    'common-objects/views/base',
    'common-objects/views/alert'
], function (require, RoleList, WorkflowListView, RolesModalView, template, deleteButton, BaseView, AlertView) {
	'use strict';
    var WorkflowContentListView = BaseView.extend({
        el: '#change-management-content',
        template: template,

        partials: {
            deleteButton: deleteButton
        },

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);

            // destroy previous content view if any
            if (WorkflowContentListView._instance) {
                WorkflowContentListView._instance.destroy();
            }

            // keep track of the created content view
            WorkflowContentListView._instance = this;

            this.events['click .actions .new'] = 'actionNew';
            this.events['click .actions .delete'] = 'actionDelete';
            this.events['click .actions .roles'] = 'actionRoles';

            this.rolesList = new RoleList();
        },
        rendered: function () {
            var _this = this;
            this.bindDomElement();
            this.listView = this.addSubView(
                new WorkflowListView({
                    el: '#list-' + this.cid
                })
            );
            this.listView.on('selectionChange', this.selectionChanged);
            this.bindCollections();

        },
        destroyed: function () {
            this.$el.html('');
        },

        bindDomElement : function(){
            this.$notifications = this.$el.find('.notifications').first();
            this.$newWorflowBtn = this.$el.find('.btn.new').first();
        },
        bindCollections : function(){
            this.listenTo(this.listView.collection, 'reset', this.onWorkflowsListChange);
            this.listView.collection.fetch({reset: true});
            this.listenTo(this.rolesList, 'reset', this.onRolesListChange);
            this.rolesList.fetch({reset: true});
        },

        selectionChanged: function () {
            var showOrHide = this.listView.checkedViews().length > 0;
            var action = showOrHide ? 'show' : 'hide';
            this.$el.find('.actions .delete')[action]();
        },

        actionNew: function () {
            this.$notifications.html('');
            App.router.navigate(App.config.workspaceId + '/workflow-model-editor', {trigger: true});
            return false;
        },
        actionDelete: function () {
            var _this = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_WORKFLOW, function(result){
                if(result){
                    _this.listView.eachChecked(function (view) {
                        view.model.destroy({
                            dataType: 'text',
                            success: _this.render
                        });
                    });
                }
            });
            return false;
        },
        actionRoles: function () {
            var _this = this;
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
        }
    });

    return WorkflowContentListView;
});
