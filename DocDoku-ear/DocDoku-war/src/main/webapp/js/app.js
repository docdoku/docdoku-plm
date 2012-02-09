var dirname = function (path) {
	return path.replace(/^.*\//g, '');
};
var app = function (config) {
	$('.collapse').collapse();
	workspace = new Workspace({id: config.workspaceId});
	foldersView = new FoldersView({
		el: $("#folders"),
		collection: workspace.folders
	});
};
