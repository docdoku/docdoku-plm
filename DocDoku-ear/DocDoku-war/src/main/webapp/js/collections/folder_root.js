RootFolderList = FolderList.extend({});
RootFolderList.prototype.__defineGetter__("url", function() {
	baseUrl = "/api/workspaces/" + app.workspaceId + "/folders"
	return  baseUrl;
}); 
