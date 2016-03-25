/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'require',
    'collections/roles',
    'text!templates/workflows/workflow_model_editor.html',
    'views/workflows/workflow_model_copy',
    'views/workflows/activity_model_editor',
    'common-objects/models/workflow/workflow_model',
    'common-objects/models/workflow/activity_model',
    'common-objects/views/alert'
], function (Backbone, Mustache, require, Roles, template, WorkflowModelCopyView, ActivityModelEditorView, WorkflowModel, ActivityModel, AlertView) {
	'use strict';
    var WorkflowModelEditorView = Backbone.View.extend({
        el: '#change-management-content',

        events: {
            'click .actions #cancel-workflow': 'cancelAction',
            'click .actions #save-workflow': 'saveAction',
            'click .actions #copy-workflow': 'copyAction',
            'click button#add-activity': 'addActivityAction'
        },

        initialize: function () {
            this.subviews = [];                                                                                         // store subview to clean them on view remove
            if(!this.roles) {
                this.roles = this.options.roles ? this.options.roles : new Roles();
            }
            this.newRoles = [];
            this.listenTo(this.roles, 'reset', this.onRolesListFetch);
        },

        render: function () {
            var _this = this;
            var templateOptions = {
                i18n: App.config.i18n
            };
            if(!_.isUndefined(this.options.workflowModelId)){
                templateOptions.workflowId = this.options.workflowModelId;
            }

            this.template = Mustache.render(template,templateOptions);
            this.$el.html(this.template);
            this.bindDomElements();

            // Init model & collection at first render
            if(!this.model){
                this.roles.fetch({
                    reset: true,
                    success:function(){
                        _this.initModel();
                    }
                });
            }

            return this;
        },
        bindDomElements: function () {
            var _this = this;
            this.inputWorkflowName = this.$('input#workflow-name');
            this.inputFinalState = this.$('input#final-state');
            this.liAddActivitySection = this.$('li#add-activity-section');
            this.$notifications = this.$el.find('.notifications').first();
            this.$saveBtn = this.$el.find('#save-workflow').first();

            this.activitiesUL = this.$('ul#activity-list');
            this.activitiesUL.sortable({
                items: 'li.activity-section',
                handle: '.activity-topbar',
                tolerance: 'pointer',
                start: function (event, ui) {
                    ui.item.oldPosition = ui.item.index();
                },
                stop: function (event, ui) {
                    _this.activityPositionChanged(ui.item.oldPosition - 1, ui.item.index() - 1);
                }
            });
        },

        initModel:function(){
            var _this = this;
            if(_.isUndefined(this.options.workflowModelId)) {
                this.model = new WorkflowModel();
                this.model.attributes.activityModels.bind('add', this.addActivity, this);

                this.$notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.WARNING_ACTIVITIES_MISSING
                }).render().$el);

                this.$notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.WARNING_FINAL_STATE_MISSING
                }).render().$el);

            } else {
                this.model = new WorkflowModel({
                    id: this.options.workflowModelId
                });
                this.model.fetch({
                    success: function(){
                        _this.onModelFetch();
                    }
                });
            }
        },
        onModelFetch: function(){

            if(!this.model.get('finalLifeCycleState')){
                this.$notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.WARNING_FINAL_STATE_MISSING
                }).render().$el);
            }

            this.inputFinalState.val(this.model.get('finalLifeCycleState'));
            this.model.attributes.activityModels.bind('add', this.addActivity, this);
            this.model.attributes.activityModels.each(this.addActivity, this);
        },
        onRolesListFetch: function(){
            var _this = this;
            this.isRolesListEmpty = _this.roles.length === 0;
            if (this.isRolesListEmpty) {
                this.$notifications.append(new AlertView({
                    type: 'warning',
                    message: App.config.i18n.WARNING_ANY_ROLE
                }).render().$el);
            }
        },
        onError:function(model, error){
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },

        addActivity: function (activityModel) {
            var _this = this;
            var activityModelEditorView = new ActivityModelEditorView({
                model: activityModel,
                roles: _this.roles,
                newRoles: _this.newRoles
            });
            _this.subviews.push(activityModelEditorView);
            activityModelEditorView.render();
            _this.liAddActivitySection.before(activityModelEditorView.el);
            _this.listenTo(activityModel, 'change', function () {
                _.each(_this.subviews, function (subview) {
                    subview.trigger('activities-order:changed');
                });
            });

            _this.listenTo(activityModel, 'destroy', function () {
                _this.subviews = _(_this.subviews).without(activityModelEditorView);
                _.each(_this.subviews, function (subview) {
                    subview.trigger('activities:removed');
                });
            });
        },

        activityPositionChanged: function (oldPosition, newPosition) {
            var activityModel = this.model.attributes.activityModels.at(oldPosition);
            this.model.attributes.activityModels.remove(activityModel, {silent: true});
            this.model.attributes.activityModels.add(activityModel, {silent: true, at: newPosition});
            _.each(this.subviews, function (subview) {
                subview.trigger('activities-order:changed');
            });
        },

        addActivityAction: function () {
            this.model.attributes.activityModels.add(new ActivityModel());
            return false;
        },
	    goToWorkflows: function () {
            App.router.navigate(App.config.workspaceId + '/workflows', {trigger: true});
        },
        cancelAction: function () {
            this.goToWorkflows();
            return false;
        },
        saveAction: function () {
            var _this = this;
            var reference = this.inputWorkflowName.val();

            if (reference) {
                if(this.newRoles.length){
                    _.each(this.newRoles, function(role){
                        role.save(null,{
                            success:function(){
                                _this.saveModel(reference);
                            },
                            error: function (model, xhr) {
                                _this.onError(model,xhr);
                            }
                        });
                    });
                } else {
                    this.saveModel(reference);
                }

            } else {
                this.inputWorkflowName.focus();
                this.onError(App.config.i18n.ERROR_WORKFLOW_REFERENCE_MISSING);
            }

            return false;
        },
        saveModel:function(reference){
            var _this = this;
            this.model.save(
                {
                    reference: reference,
                    finalLifeCycleState: _this.inputFinalState.val()
                },
                {
                    success: function () {
                        _this.goToWorkflows();
                    },
                    error: function (model, xhr) {
                        _this.onError(model,xhr);
                        _this.inputWorkflowName.focus();
                    }
                }
            );
        },
        copyAction: function () {

            var workflowModelCopyView = new WorkflowModelCopyView({
                model: this.model
            });

            window.document.body.appendChild(workflowModelCopyView.render().el);

            workflowModelCopyView.openModal();

            return false;
        },

        unbindAllEvents: function () {
            _.each(this.subviews, function (subview) {
                subview.unbindAllEvents();
            });
            this.undelegateEvents();
        }
    });

    return WorkflowModelEditorView;
});
