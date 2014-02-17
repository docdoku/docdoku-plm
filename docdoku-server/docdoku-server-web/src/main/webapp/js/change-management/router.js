define([
	"common-objects/common/singleton_decorator",
	"views/workflow_nav",
    "views/workflow_model_editor"
],
function (
	singletonDecorator,
	WorkflowNavView,
    WorkflowModelEditorView
) {
	var Router = Backbone.Router.extend({
		routes: {
			"workflows":		"workflows",
            "workflow-model-editor/:workflowModelId":  "workflowModelEditor",
            "workflow-model-editor":  "workflowModelEditorNew",
			"":					"defaults"
		},
		workflows: function() {
			this.defaults();
			var view = WorkflowNavView.getInstance();
			view.showContent();
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
		}
	});
	Router = singletonDecorator(Router);
	return Router;
});
