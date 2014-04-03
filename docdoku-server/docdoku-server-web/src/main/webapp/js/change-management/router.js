define([
	"common-objects/common/singleton_decorator",
	"views/nav/workflow_nav",
    "views/nav/milestone_nav",
    "views/nav/change_issue_nav",
    "views/nav/change_request_nav",
    "views/nav/change_order_nav",
    "views/workflows/workflow_model_editor"
],
function (
	singletonDecorator,
	WorkflowNavView,
	MilestoneNavView,
    ChangeIssueNavView,
    ChangeRequestNavView,
    ChangeOrderNavView,
    WorkflowModelEditorView
) {
	var Router = Backbone.Router.extend({
        contentSelector: "#change-management-content",
		routes: {
			"workflows":"workflows",
            "milestones":"milestones",
            "issues":"issues",
            "requests":"requests",
            "orders":"orders",
            "workflow-model-editor/:workflowModelId":"workflowModelEditor",
            "workflow-model-editor":"workflowModelEditorNew",
			"":"defaults"
		},

		workflows: function() {
			this.defaults();
			WorkflowNavView.getInstance().showContent();
		},

        milestones: function() {
            this.defaults();
            MilestoneNavView.getInstance().showContent(this.contentSelector);
        },

        issues: function() {
            this.defaults();
            this.cleanContent();
            ChangeIssueNavView.getInstance().showContent(this.contentSelector);
        },
        requests: function() {
            this.defaults();
            this.cleanContent();
            ChangeRequestNavView.getInstance().showContent(this.contentSelector);
        },
        orders: function() {
            this.defaults();
            this.cleanContent();
            ChangeOrderNavView.getInstance().showContent(this.contentSelector);
        },

        workflowModelEditor: function(workflowModelId) {
            this.defaults();

            if(!_.isUndefined(this.workflowModelEditorView)){
                this.workflowModelEditorView.unbindAllEvents();
            }

            this.workflowModelEditorView = new WorkflowModelEditorView({
                workflowModelId: decodeURI(workflowModelId)
            });

            this.workflowModelEditorView.render();
        },

        workflowModelEditorNew: function() {
            this.defaults();

            if(!_.isUndefined(this.workflowModelEditorView)){
                this.workflowModelEditorView.unbindAllEvents();
            }

            this.workflowModelEditorView = new WorkflowModelEditorView();

            this.workflowModelEditorView.render();
        },

		defaults: function() {
			WorkflowNavView.getInstance();
            MilestoneNavView.getInstance();
            ChangeIssueNavView.getInstance();
            ChangeOrderNavView.getInstance();
            ChangeRequestNavView.getInstance();
		},

        cleanContent: function() {
            MilestoneNavView.getInstance().cleanView();
            ChangeIssueNavView.getInstance().cleanView();
            ChangeOrderNavView.getInstance().cleanView();
            ChangeRequestNavView.getInstance().cleanView();
        }
	});
	Router = singletonDecorator(Router);
	return Router;
});
