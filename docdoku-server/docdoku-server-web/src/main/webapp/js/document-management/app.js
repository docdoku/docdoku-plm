function debug(str){
    console.log(str);
}

define([
	"router",
	"models/workspace",
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
		el: "#workspace",
		model: this.workspace
	}).render();

    new NavBarView();

	Router.getInstance();
	Backbone.history.start();
});