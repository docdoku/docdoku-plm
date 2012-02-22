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
	}
});
// Folder.documents getter an setter
Folder.prototype.__defineGetter__("documents", function() {
	if (!this._documents) {
		this._documents = new DocumentList();
		this._documents.url = "/api/documents/" + app.workspaceId
	}
	return this._documents;
}); 
Folder.prototype.__defineSetter__("documents", function(value) {
	this._documents = value
}); 

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
	}
});
