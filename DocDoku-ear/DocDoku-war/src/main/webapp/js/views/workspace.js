var WorkspaceView = BaseView.extend({
	template: "workspace-tpl",
	rendered: function () {
		FolderNavView.getInstance();
	},
});
