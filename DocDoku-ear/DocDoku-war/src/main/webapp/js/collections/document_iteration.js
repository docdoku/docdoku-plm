var DocumentIterationList = Backbone.Collection.extend({
	model: DocumentIteration,
	initialize: function (models, options) {
		this.url = "/api/workspaces/" + app.workspaceId + "/documents/" + options.document.id + "/iterations";
	}
});
