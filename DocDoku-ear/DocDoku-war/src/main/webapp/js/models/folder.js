var Folder = Backbone.Model.extend({
	url: function() {
		if (this.get("id")) {
			return "/api/workspaces/" + app.workspaceId + "/folders/" + this.get("id");
		} else if (this.collection) {
			return this.collection.url;
		}
	},
	completePath: function() {
		return this.get("path") + "/" + this.get("name");
	},
	initialize: function () {
		this.folders = new FolderList();
		this.folders.parent = this;
		this.documents = new DocumentList();
		this.documents.parent = this;
	}
});
