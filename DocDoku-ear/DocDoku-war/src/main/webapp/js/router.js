var Router = Backbone.Router.extend({
	routes: {
		"templates":	"templates",
		"checkedouts":	"checkedouts",
		"":				"default",
	},
	templates: function() {
		var collection = new TemplateList();
		new TemplateListView({
			el: $("#content"),
			collection: collection
		});
		$("html, body").animate({ scrollTop: 0 }, "fast");
	},
	checkedouts: function() {
		var collection = new DocumentCheckedoutList();
		new DocumentCheckedoutListView({
			el: $("#content"),
			collection: collection
		});
		$("html, body").animate({ scrollTop: 0 }, "fast");
	},
	default: function() {
		console.debug("Router.default");
	},
});
