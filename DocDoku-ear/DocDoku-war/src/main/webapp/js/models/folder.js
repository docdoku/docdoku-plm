var Folder = Backbone.Model.extend({
	path: function() {
		return app.restpath(this.get("completePath"));
	}
});
// Folder.folders getter an setter
Folder.prototype.__defineGetter__("folders", function() {
	if (!this._folders) {
		this._folders = new FolderList();
		this._folders.url = "/api/folders/" + this.get("completePath");
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
	initialize: function () {
		this.set({
			name: "Documents",
			completePath: app.workspaceId
		});
		this.urlRoot = "/api/folders/" + app.workspaceId;
	}
});
