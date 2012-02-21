var Folder = Backbone.Model.extend({
	url: function () {
		return "/api/workspaces/" + app.workspaceId + "/folders/" + this.get("id");
	},
	completePath: function() {
		return this.get("path") + "/" + this.get("name");
	}
});
// Folder.folders getter an setter
Folder.prototype.__defineGetter__("folders", function() {
	if (!this._folders) {
		this._folders = new FolderList();
		this._folders.url = "/api/workspaces/" + app.workspaceId + "/folders/" + this.get("id") + "/folders";
	}
	return this._folders;
}); 
Folder.prototype.__defineSetter__("folders", function(value) {
	this._folders = value;
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
		//this.urlRoot = "/api/folders/" + app.workspaceId;
	}
});
RootFolder.prototype.__defineGetter__("folders", function() {
	if (!this._folders) {
		this._folders = new FolderList();
		this._folders.url = "/api/workspaces/" + app.workspaceId + "/folders";
	}
	return this._folders;
}); 
