RootDocumentList = DocumentList.extend({});
RootDocumentList.prototype.__defineGetter__("url", function() {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/documents";
	return baseUrl;
}); 
