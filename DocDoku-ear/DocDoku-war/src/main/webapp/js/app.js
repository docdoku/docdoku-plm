var app = {
	init: function (config) {
		this.workspaceId = config.workspaceId
		this.login = config.login
		this.workspace = new Workspace({
			id: this.workspaceId
		});

		$('.collapse').collapse();
		$('.dropdown-toggle').dropdown();
		$('.modal').modal();

		workspaceView = new WorkspaceView({
			el: $("#workspace"),
			model: this.workspace
		});
		workspaceView.render();
	},
	dirname: function (path) {
		return path.replace(/\/[^\/]*$/g, '');
	},
	basename: function (path) {
		return path.replace(/^.*\//g, '');
	},
	// Returns the rest of the given path
	restpath: function(completePath) {
		var path = completePath.split("/");
		path.shift();
		return path.join("/");
	},
	i18n: {}
}
