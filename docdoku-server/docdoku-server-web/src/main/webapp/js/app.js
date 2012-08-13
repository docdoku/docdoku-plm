function debug(str){
    console.log("DEBUG >> "+str);
}

define([
	"router",
	"models/workspace",
	"views/workspace"
], function (
	Router,
	Workspace,
	WorkspaceView
) {
	var workspace = new Workspace({
		id: APP_CONFIG.workspaceId
	});
	new WorkspaceView({
		el: "#workspace",
		model: this.workspace
	}).render();

	Router.getInstance();
	Backbone.history.start();
});
