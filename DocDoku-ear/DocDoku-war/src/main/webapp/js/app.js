var dirname = function (path) {
	return path.replace(/^.*\//g, '');
};
var app = function (config) {
	$('.collapse').collapse();
	workspace = new Workspace({
		id: config.workspaceId,
		login: config.login
	});
	rootFolderView = new FolderView({
		model: new Folder({
			name: "Dossiers"
		}),
		collection: workspace.folders
	});
	$("#folders").append(rootFolderView.el);
};
