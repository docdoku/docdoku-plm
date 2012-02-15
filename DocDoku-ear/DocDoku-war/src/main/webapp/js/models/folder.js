var Folder = Backbone.Model.extend({
	path: function() {
		return this.get("completePath").replace(/^[^\/]*\//g, '');
	}
});
// Folder.urlRoot getter an setter
Folder.prototype.__defineGetter__("urlRoot", function() {
	if (this.isRoot && !this._urlRoot) {
		this._urlRoot = "/api/folders/" + app.workspaceId;
	}
	return this._urlRoot;
}); 
Folder.prototype.__defineSetter__("urlRoot", function(value) {
	this._urlRoot = value;
}); 
// Folder.folders getter an setter
Folder.prototype.__defineGetter__("folders", function() {
	if (!this._folders) {
		this._folders = new FolderList();
		this._folders.url = "/api/folders/" + this.get("completePath");
		if (this.isRoot) {
			this._folders.home =  app.workspaceId + "/~" + app.login;
		}
	}
	return this._folders;
}); 
Folder.prototype.__defineSetter__("folders", function(value) {
	this._folders = value;
}); 
