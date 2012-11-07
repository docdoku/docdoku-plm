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
