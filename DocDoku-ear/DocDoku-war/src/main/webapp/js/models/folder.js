var Folder = Backbone.Model.extend({
	defaults: {
		home: false
	},
	url: function() {
		if (this.get("id")) {
			return "/api/workspaces/" + app.workspaceId + "/folders/" + this.get("id");
		} else if (this.collection) {
			return this.collection.url;
		}
	},
});
