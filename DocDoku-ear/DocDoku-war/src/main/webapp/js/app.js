var dirname = function (path) {
	return path.replace(/^.*\//g, '');
};
var app = function (config) {
	FolderList.workspaceId = config.workspaceId
	FolderList.login = config.login
	$('.collapse').collapse();
	$('.dropdown-toggle').dropdown();
	workspace = new Workspace({
		id: config.workspaceId,
		login: config.login
	});
	rootFolder = new Folder({name:"Documents"});
	rootFolder.folders = workspace.folders;
	rootFolderView = new FolderView({model: rootFolder});
	$("#folders").append(rootFolderView.el);
};
