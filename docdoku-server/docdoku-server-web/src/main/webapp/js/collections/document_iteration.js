define([
	"models/document_iteration"
], function (
	DocumentIteration
) {
	var DocumentIterationList = Backbone.Collection.extend({
		model: DocumentIteration,




		url: function () {
			return "/api/workspaces/"
				+ APP_CONFIG.workspaceId
				+ "/documents/"
				+ this.document.id
				+ "/iterations";
		}
	});
    DocumentIterationList.className="DocumentIterationList";
	return DocumentIterationList;
});
