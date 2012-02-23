WorkspaceView = BaseView.extend({
	template_el: "#workspace-tpl",
	initialize: function () {
		_.bindAll(this,
			"template", "render");
	},
	render: function () {
		$(this.el).html(this.template({
			model: this.model.toJSON()
		}));
		rootFolderView = new FolderView({model:new RootFolder()});
		$("#folders").append(rootFolderView.el);
		return this;
	},
});
