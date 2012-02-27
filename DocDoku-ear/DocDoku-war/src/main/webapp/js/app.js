var app = {
	init: function (config) {
		this.workspaceId = config.workspaceId;
		this.login = config.login;
		this.workspace = new Workspace({
			id: this.workspaceId
		});

		$(".collapse").collapse();
		$(".dropdown-toggle").dropdown();
		$(".modal").modal();
		$(".alert").alert();

		workspaceView = new WorkspaceView({
			el: $("#workspace"),
			model: this.workspace
		});
		workspaceView.render();
		this.router = new Router();
		Backbone.history.start();
	},
	i18n: {}
}
