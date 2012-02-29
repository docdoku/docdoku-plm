var Router = Backbone.Router.extend({
	routes: {
		"templates":	"templates",
		"checkedout":	"checkedout",
		"":				"default",
	},
	templates: function() {
		collection = new TemplateList();
		new TemplateListView({
			el: $("#workspace .content"),
			collection: collection
		});
		$("html, body").animate({ scrollTop: 0 }, "slow");
	},
	checkedout: function() {
		collection = new DocumentCheckedoutList();
		new DocumentCheckedoutListView({
			el: $("#workspace .content"),
			collection: collection
		});
		$("html, body").animate({ scrollTop: 0 }, "slow");
	},
	default: function() {
		console.debug("Router.default");
	},
});
