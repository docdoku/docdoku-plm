define([
        "backbone",
        "common-objects/common/singleton_decorator",
        "views/nav/workflow_nav",
        "views/nav/milestone_nav",
        "views/nav/change_issue_nav",
        "views/nav/change_request_nav",
        "views/nav/change_order_nav",
        "views/workflows/workflow_model_editor"
    ],
    function (Backbone,singletonDecorator, WorkflowNavView, MilestoneNavView, ChangeIssueNavView, ChangeRequestNavView, ChangeOrderNavView, WorkflowModelEditorView) {
        var Router = Backbone.Router.extend({
            contentSelector: "#change-management-content",
            routes: {
                ":workspaceId/workflows": "workflows",
                ":workspaceId/milestones": "milestones",
                ":workspaceId/issues": "issues",
                ":workspaceId/requests": "requests",
                ":workspaceId/orders": "orders",
                ":workspaceId/workflow-model-editor/:workflowModelId": "workflowModelEditor",
                ":workspaceId/workflow-model-editor": "workflowModelEditorNew",
                ":workspaceId": "defaults"
            },

            workflows: function (workspaceId) {
                this.defaults(workspaceId);
                WorkflowNavView.getInstance().showContent();
            },

            milestones: function (workspaceId) {
                this.defaults(workspaceId);
                MilestoneNavView.getInstance().showContent(this.contentSelector);
            },

            issues: function (workspaceId) {
                this.defaults(workspaceId);
                this.cleanContent();
                ChangeIssueNavView.getInstance().showContent(this.contentSelector);
            },
            requests: function (workspaceId) {
                this.defaults(workspaceId);
                this.cleanContent();
                ChangeRequestNavView.getInstance().showContent(this.contentSelector);
            },
            orders: function (workspaceId) {
                this.defaults(workspaceId);
                this.cleanContent();
                ChangeOrderNavView.getInstance().showContent(this.contentSelector);
            },

            workflowModelEditor: function (workspaceId, workflowModelId) {
                this.defaults(workspaceId);

                if (!_.isUndefined(this.workflowModelEditorView)) {
                    this.workflowModelEditorView.unbindAllEvents();
                }

                this.workflowModelEditorView = new WorkflowModelEditorView({
                    workflowModelId: decodeURI(workflowModelId)
                });

                this.workflowModelEditorView.render();
            },

            workflowModelEditorNew: function (workspaceId) {
                this.defaults(workspaceId);

                if (!_.isUndefined(this.workflowModelEditorView)) {
                    this.workflowModelEditorView.unbindAllEvents();
                }

                this.workflowModelEditorView = new WorkflowModelEditorView();

                this.workflowModelEditorView.render();
            },

            defaults: function (workspaceId) {

                if (workspaceId != APP_CONFIG.workspaceId) {
                    location.reload();
                    return;
                }

                WorkflowNavView.getInstance();
                MilestoneNavView.getInstance();
                ChangeIssueNavView.getInstance();
                ChangeOrderNavView.getInstance();
                ChangeRequestNavView.getInstance();
            },

            cleanContent: function () {
                MilestoneNavView.getInstance().cleanView();
                ChangeIssueNavView.getInstance().cleanView();
                ChangeOrderNavView.getInstance().cleanView();
                ChangeRequestNavView.getInstance().cleanView();
            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
