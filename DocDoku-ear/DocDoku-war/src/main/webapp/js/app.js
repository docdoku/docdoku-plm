var dirname = function (path) {
	return path.replace(/^.*\//g, '');
};
var basename = function (path) {
	return path.replace(/\/[^\/]*$/g, '');
};
var app = function (config) {
	FolderList.workspaceId = config.workspaceId
	FolderList.login = config.login
	$('.collapse').collapse();
	$('.dropdown-toggle').dropdown();
	$('.modal').modal();
	workspace = new Workspace({
		id: config.workspaceId,
		login: config.login
	});
	rootFolder = new Folder({
		name:"Documents",
		completePath: config.workspaceId,
		
	});
	rootFolder.urlRoot = workspace.folders.url;
	rootFolder.folders = workspace.folders;
	rootFolderView = new FolderView({model: rootFolder});
	$("#folders").append(rootFolderView.el);
};
