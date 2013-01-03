define([
	"common/singleton_decorator",
	"views/folder_nav",
	"views/tag_nav",
	"views/workflow_nav",
	"views/template_nav",
	"views/checkedout_nav",
    "views/workflow_model_editor",
    "models/workflow_model"
],
function (
	singletonDecorator,
	FolderNavView,
	TagNavView,
	WorkflowNavView,
	TemplateNavView,
	CheckedoutNavView,
    WorkflowModelEditorView
) {
	var Router = Backbone.Router.extend({
		routes: {
			"folders":			"folders",
			"folders/*path":	"folder",
			"tags":				"tags",
			"tags/:id": 		"tag",
			"templates":		"templates",
			"workflows":		"workflows",
            "workflow-model-editor/:workflowModelId":  "workflowModelEditor",
            "workflow-model-editor":  "workflowModelEditorNew",
			"checkedouts":		"checkedouts",
			"tasks":			"tasks",
			"":					"defaults"
		},
		folders: function() {
			this.defaults();
			FolderNavView.getInstance().toggle();
		},
		folder: function(path) {
			this.defaults();
			FolderNavView.getInstance().show(path);
		},
		tags: function() {
			this.defaults();
			TagNavView.getInstance().toggle();
		},
		tag: function(id) {
			this.defaults();
			TagNavView.getInstance().show(id);
		},
		workflows: function() {
			this.defaults();
			var view = WorkflowNavView.getInstance();
			view.showContent();
		},
        workflowModelEditor: function(workflowModelId) {
            this.defaults();

            if(!_.isUndefined(this.workflowModelEditorView))
                this.workflowModelEditorView.unbindAllEvents();

            this.workflowModelEditorView = new WorkflowModelEditorView({
                workflowModelId: workflowModelId
            });

            this.workflowModelEditorView.render();
        },
        workflowModelEditorNew: function() {
            this.defaults();

            if(!_.isUndefined(this.workflowModelEditorView))
                this.workflowModelEditorView.unbindAllEvents();

            this.workflowModelEditorView = new WorkflowModelEditorView();

            this.workflowModelEditorView.render();
        },
		templates: function() {
			this.defaults();
			var view = TemplateNavView.getInstance();
			view.showContent();
		},
		checkedouts: function() {
			this.defaults();
			var view = CheckedoutNavView.getInstance();
			view.showContent();
		},
		tasks: function() {
			this.defaults();
		},
		defaults: function() {
			FolderNavView.getInstance();
			TagNavView.getInstance();
			WorkflowNavView.getInstance();
			TemplateNavView.getInstance();
			CheckedoutNavView.getInstance();
		}
	});
	Router = singletonDecorator(Router);
	return Router;
});
