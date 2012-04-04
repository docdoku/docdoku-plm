var app = {
	init: function (config) {
		this.workspaceId = config.workspaceId;
		this.login = config.login;
		this.initTemplates();
		this.initPartials();
		this.initWorkspace();
		this.router = new Router();
		Backbone.history.start();
	},
	initTemplates: function () {
		var templates = {};
		$("script.template").each( function () {
			templates[this.id] = Mustache.compile(this.innerHTML);
		});
		this.templates = templates;
	},
	initPartials: function () {
		var partials = {};
		$("script.partial").each( function () {
			partials[this.id] = this.innerHTML;
		});
		this.partials = partials;
	},
	initWorkspace: function () {
		this.workspace = new Workspace({
			id: this.workspaceId
		});
		new WorkspaceView({
			el: "#workspace",
			model: this.workspace
		}).render();
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
};
