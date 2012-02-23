var app = {
	init: function (config) {
		this.workspaceId = config.workspaceId
		this.login = config.login

		$('.collapse').collapse();
		$('.dropdown-toggle').dropdown();
		$('.modal').modal();

		workspaceView = new WorkspaceView({
			el: $("#workspace"),
			model: new Workspace({
				id: this.workspaceId
			})
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
