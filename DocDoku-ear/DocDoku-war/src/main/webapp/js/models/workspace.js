var Workspace = Backbone.Model.extend({
	initialize: function () {
		this.folders = new FolderList();
		this.folders.url = "/api/folders/" + this.id;
		this.folders.home =  this.id + "/~" + this.get("login");
	}
});
