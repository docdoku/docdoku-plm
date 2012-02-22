RootFolder = Folder.extend({
	completePath: function() {
		return this.get("path") + "/" + app.workspaceId;
	},
	initialize: function () {
		this.set({
			name: "Documents",
			path: ""
		});
		this.folders = new RootFolderList();
		this.folders.parent = this;
		this.documents = new RootDocumentList();
		this.documents.parent = this;
	}
});
