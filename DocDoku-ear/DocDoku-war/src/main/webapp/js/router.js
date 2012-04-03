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
		app.scrollTop();
	},
});
