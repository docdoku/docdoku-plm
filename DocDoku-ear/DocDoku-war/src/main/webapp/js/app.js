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
		// Compile all templates at init time
		var templates = {};
		$("script.template").each( function () {
			templates[this.id] = Mustache.compile(this.innerHTML);
		});
		this.templates = templates;
	},
	initPartials: function () {
		// Compile all partial templates at init time
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
	i18n: {}, // Filled by i18n.js
	formatDate: function (unformatedDate) {
		// TODO: use moments.js ?
		try {
			var formatedDate = new Date(unformatedDate).format("dd/mm/yyyy");
			return formatedDate;
		} catch (error) {
			console.error("app:formatDate(" + unformatedDate + ")", error);
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
