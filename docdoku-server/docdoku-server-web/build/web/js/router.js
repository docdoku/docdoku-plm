define([
	"require",
	"common/singleton_decorator",
	"views/folder_nav",
	"views/tag_nav",
	"views/workflow_nav",
	"views/template_nav",
	"views/checkedout_nav"
],
function (
	require,
	singletonDecorator,
	FolderNavView,
	TagNavView,
	WorkflowNavView,
	TemplateNavView,
	CheckedoutNavView
) {
	var Router = Backbone.Router.extend({
		routes: {
			"folders":			"folders",
			"folders/*path":	"folder",
			"tags":				"tags",
			"tags/:id": 		"tag",
			"templates":		"templates",
			"workflows":		"workflows",
			"checkedouts":		"checkedouts",
			"tasks":			"tasks",
			"":					"default",
		},
		folders: function() {
			this.default();
			FolderNavView.getInstance().toggle();
		},
		folder: function(path) {
			this.default();
			FolderNavView.getInstance().show(path);
		},
		tags: function() {
			this.default();
			TagNavView.getInstance().toggle();
		},
		tag: function(id) {
			this.default();
			TagNavView.getInstance().show(id);
		},
		workflows: function() {
			this.default();
			var view = WorkflowNavView.getInstance();
			view.showContent();
		},
		templates: function() {
			this.default();
			var view = TemplateNavView.getInstance();
			view.showContent();
		},
		checkedouts: function() {
			this.default();
			var view = CheckedoutNavView.getInstance();
			view.showContent();
		},
		tasks: function() {
			this.default();
		},
		default: function() {
			FolderNavView.getInstance();
			TagNavView.getInstance();
			WorkflowNavView.getInstance();
			TemplateNavView.getInstance();
			CheckedoutNavView.getInstance();
		},
	});
	Router = singletonDecorator(Router);
	return Router;
});
