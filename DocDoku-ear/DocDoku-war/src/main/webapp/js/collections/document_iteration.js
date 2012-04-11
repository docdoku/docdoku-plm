var DocumentIterationList = Backbone.Collection.extend({
	model: DocumentIteration,
	url: function () {
		return "/api/workspaces/"
			+ app.workspaceId
			+ "/documents/"
			+ this.document.id
			+ "/iterations";
	},
});
