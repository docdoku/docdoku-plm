/*global _,define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator',
    'views/nav/workflow_nav',
    'views/nav/task_nav',
    'views/nav/milestone_nav',
    'views/nav/change_issue_nav',
    'views/nav/change_request_nav',
    'views/nav/change_order_nav',
    'views/workflows/workflow_model_editor'
],
function (Backbone,singletonDecorator, WorkflowNavView, TaskNavView, MilestoneNavView, ChangeIssueNavView, ChangeRequestNavView, ChangeOrderNavView, WorkflowModelEditorView) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/workflows': 'workflows',
            ':workspaceId/milestones': 'milestones',
            ':workspaceId/issues': 'issues',
            ':workspaceId/requests': 'requests',
            ':workspaceId/orders': 'orders',
            ':workspaceId/workflow-model-editor/:workflowModelId': 'workflowModelEditor',
            ':workspaceId/workflow-model-editor': 'workflowModelEditorNew',
            ':workspaceId/tasks/:taskId': 'task',
            ':workspaceId/workflow/:workflowId': 'workflow',
            ':workspaceId': 'workflows'
        },

	    executeOrReload:function(workspaceId,fn){
		    if(workspaceId !== App.config.workspaceId && decodeURIComponent(workspaceId).trim() !== App.config.workspaceId) {
			    location.reload();
		    }else{
			    fn.bind(this).call();
		    }
	    },

	    initNavViews: function () {
		    WorkflowNavView.getInstance();
		    MilestoneNavView.getInstance();
		    ChangeIssueNavView.getInstance();
		    ChangeOrderNavView.getInstance();
		    ChangeRequestNavView.getInstance();
		    TaskNavView.getInstance();
	    },

	    cleanContent: function () {
            WorkflowNavView.getInstance().cleanView();
            MilestoneNavView.getInstance().cleanView();
		    ChangeIssueNavView.getInstance().cleanView();
		    ChangeOrderNavView.getInstance().cleanView();
		    ChangeRequestNavView.getInstance().cleanView();
            TaskNavView.getInstance().cleanView();
	    },

        workflows: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            WorkflowNavView.getInstance().showContent();
            });
        },
        milestones: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            MilestoneNavView.getInstance().showContent();
            });
        },
        issues: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            this.cleanContent();
	            ChangeIssueNavView.getInstance().showContent();
            });
        },
        requests: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            this.cleanContent();
	            ChangeRequestNavView.getInstance().showContent();
            });
        },
        orders: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            this.cleanContent();
	            ChangeOrderNavView.getInstance().showContent();
            });
        },

        workflowModelEditor: function (workspaceId, workflowModelId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            if (!_.isUndefined(this.workflowModelEditorView)) {
		            this.workflowModelEditorView.unbindAllEvents();
	            }

	            this.workflowModelEditorView = new WorkflowModelEditorView({
		            workflowModelId: decodeURI(workflowModelId)
	            });

	            this.workflowModelEditorView.render();
            });
		},

        workflowModelEditorNew: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            if (!_.isUndefined(this.workflowModelEditorView)) {
		            this.workflowModelEditorView.unbindAllEvents();
	            }

	            this.workflowModelEditorView = new WorkflowModelEditorView();

	            this.workflowModelEditorView.render();
            });
        },

        task:function(workspaceId, taskId){
            this.executeOrReload(workspaceId,function(){
                this.initNavViews();
                this.cleanContent();
                TaskNavView.getInstance().showTaskContent(taskId);
            });
        },

        workflow:function(workspaceId, workflowId){
            this.executeOrReload(workspaceId,function(){
                this.initNavViews();
                this.cleanContent();
                TaskNavView.getInstance().showWorkflowContent(workflowId);
            });
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
