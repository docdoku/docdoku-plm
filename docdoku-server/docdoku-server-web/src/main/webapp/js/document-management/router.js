define([
	"common-objects/common/singleton_decorator",
	"views/folder_nav",
	"views/tag_nav",
	"views/search_nav",
	"views/template_nav",
	"views/checkedout_nav",
	"views/task_nav"
],
function (
	singletonDecorator,
	FolderNavView,
	TagNavView,
	SearchNavView,
	TemplateNavView,
	CheckedoutNavView,
	TaskNavView
) {
	var Router = Backbone.Router.extend({
		routes: {
			"folders":			"folders",
			"folders/*path":	"folder",
			"tags":				"tags",
			"tags/:id":         "tag",
			"templates":		"templates",
			"checkedouts":		"checkedouts",
			"tasks":	        "tasks",
			"tasks/:filter":	"tasks",
			"search/:query":	"search",
			"":					"defaults"
		},
		folders: function() {
			this.defaults();
			FolderNavView.getInstance().toggle();
		},
		folder: function(path) {
			this.defaults();
            FolderNavView.getInstance().show(decodeURIComponent(path));
        },
		tags: function() {
			this.defaults();
			TagNavView.getInstance().toggle();
		},
		tag: function(id) {
			this.defaults();
			TagNavView.getInstance().show(id);
		},
		templates: function() {
			this.defaults();
			var view = TemplateNavView.getInstance();
			view.showContent();
		},
		checkedouts: function() {
			this.defaults();
			CheckedoutNavView.getInstance().showContent();
		},
		tasks: function(filter) {
			this.defaults();
            TaskNavView.getInstance().showContent(filter);
		},
        search: function(query) {
            this.defaults();
            SearchNavView.getInstance().showContent(query);
        },
		defaults: function() {
			FolderNavView.getInstance();
			TagNavView.getInstance();
			TemplateNavView.getInstance();
			CheckedoutNavView.getInstance();
            SearchNavView.getInstance();
            TaskNavView.getInstance();
		}
	});
	Router = singletonDecorator(Router);
	return Router;
});
