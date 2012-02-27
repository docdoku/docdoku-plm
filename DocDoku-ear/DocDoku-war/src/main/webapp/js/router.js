var Router = Backbone.Router.extend({
	routes: {
		"templates":	"templates",
		"checkedout":	"checkedout",
		"/":	"default",
	},
	templates: function() {
		collection = new TemplateList();
		new TemplateListView({
			el: $("#workspace .content"),
			collection: collection
		});
	},
	default: function() {
		console.debug("route: default");
	},
});
