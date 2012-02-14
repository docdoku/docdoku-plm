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
		id:"",
		name:"Documents",
		completePath: config.workspaceId
	});
	rootFolder.url = function () { return workspace.folders.url }; // TODO: Hack. Must be fixed
	rootFolder.folders = workspace.folders;
	rootFolderView = new FolderView({model: rootFolder});
	$("#folders").append(rootFolderView.el);
};
