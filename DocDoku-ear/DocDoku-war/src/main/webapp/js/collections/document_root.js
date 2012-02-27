RootDocumentList = DocumentList.extend({
	url: function () {
		baseUrl = "/api/workspaces/" + app.workspaceId + "/documents";
		return baseUrl;
	}
});
