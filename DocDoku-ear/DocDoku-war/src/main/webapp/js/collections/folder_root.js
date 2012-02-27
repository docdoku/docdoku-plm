RootFolderList = FolderList.extend({
	url: function () {
		baseUrl = "/api/workspaces/" + app.workspaceId + "/folders"
		return  baseUrl;
	},
});
