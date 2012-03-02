var Router = Backbone.Router.extend({
	routes: {
		"templates":	"templates",
		"workflows":	"workflows",
		"checkedouts":	"checkedouts",
		"":				"default",
	},
	templates: function() {
		if (app.contentView) app.contentView.remove();
		delete app.contentView;
		var collection = new TemplateList();
		var view = new TemplateListView({
			el: $("#content"),
			collection: collection
		});
		app.contentView = view;
		$("html, body").animate({ scrollTop: 0 }, "fast");
	},
	workflows: function() {
		if (app.contentView) app.contentView.remove();
		delete app.contentView;
		var collection = new WorkflowList();
		var view = new WorkflowListView({
			el: $("#content"),
			collection: collection
		});
		app.contentView = view;
		$("html, body").animate({ scrollTop: 0 }, "fast");
	},
	checkedouts: function() {
		if (app.contentView) app.contentView.remove();
		delete app.contentView;
		var collection = new DocumentCheckedoutList();
		var view = new DocumentCheckedoutListView({
			el: $("#content"),
			collection: collection
		});
		app.contentView = view;
		$("html, body").animate({ scrollTop: 0 }, "fast");
	},
	default: function() {
		console.debug("Router.default");
	},
});
