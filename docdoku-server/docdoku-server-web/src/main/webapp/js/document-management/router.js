define([
	"common-objects/common/singleton_decorator",
	"views/folder_nav",
	"views/tag_nav",
	"views/search_nav",
	"views/workflow_nav",
	"views/template_nav",
	"views/checkedout_nav",
	"views/task_nav",
    "views/workflow_model_editor"
],
function (
	singletonDecorator,
	FolderNavView,
	TagNavView,
	SearchNavView,
	WorkflowNavView,
	TemplateNavView,
	CheckedoutNavView,
	TaskNavView,
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
			"search/:query":	"search",
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
                workflowModelId: decodeURI(workflowModelId)
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
            TaskNavView.getInstance().showContent();
		},
        search: function(query) {
            this.defaults();
            SearchNavView.getInstance().showContent(query);
        },
		defaults: function() {
			FolderNavView.getInstance();
			TagNavView.getInstance();
			WorkflowNavView.getInstance();
			TemplateNavView.getInstance();
			CheckedoutNavView.getInstance();
            SearchNavView.getInstance();
            TaskNavView.getInstance();
		}
	});
	Router = singletonDecorator(Router);
	return Router;
});
