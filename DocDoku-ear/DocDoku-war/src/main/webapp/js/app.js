var app = {
	init: function (config) {
		this.workspaceId = config.workspaceId;
		this.login = config.login;
		this.workspace = new Workspace({
			id: this.workspaceId
		});

		//$(".dropdown-toggle").dropdown();
		//$(".modal").modal();
		//$(".collapse").collapse();
		//$(".alert").alert();

		var workspaceView = new WorkspaceView({
			el: $("#workspace"),
			model: this.workspace
		});
		workspaceView.render();
		this.router = new Router();
		Backbone.history.start();
	},
	i18n: {},
	formatDate: function (unformatedDate) {
		try {
			var formatedDate = new Date(unformatedDate).format("dd/mm/yyyy");
			return formatedDate;
		} catch (error) {
			console.error("app:formatDate", error);
			return unformatedDate;
		}
	},
	scrollTo: function (el) {
		var offset = el ? $(el).offset().top : 0;
		$("html, body").animate({ scrollTop: offset }, "fast");
	},
	scrollTop: function () {
		this.scrollTo();
	},
}
