define([
	"router",
	"common-objects/models/workspace",
	"views/workspace",
    "modules/navbar-module/views/navbar_view"
], function (
	Router,
	Workspace,
	WorkspaceView,
    NavBarView
) {
	var workspace = new Workspace({
		id: APP_CONFIG.workspaceId
	});

    new WorkspaceView({
		el: "#content",
		model: workspace
	}).render().menuResizable();

    new NavBarView();

	Router.getInstance();
	Backbone.history.start();
});