var FolderList = Backbone.Collection.extend({
	model: Folder,
	parse: function(data) {
		// adding folders' id from completePath
		// done here because completePath is not reachable in initialize
		var folders = [];
		if (this.url == "/api/workspaces/" + app.workspaceId +"/folders") {
			data.unshift({
				id: window.btoa(app.workspaceId + "/~" + app.login),
				name: "~" + app.login,
				path: app.workspaceId,
				home: true
			});
		}
		_.each(data, function (item) {
			folders.push({
				id: item.id,
				name: item.name,
				path: item.path,
				home: item.home ? item.home : false
			});	    		
		});
		return folders; 
	},
	comparator: function (folderA, folderB) {
		nameA = folderA.get("name");
		nameB = folderB.get("name");

		if (folderB.get("home")) return 1;
		if (folderA.get("home")) return -1;
		if (nameA == nameB) return 0;
		return (nameA < nameB) ? -1 : 1;
	}
});
// FolderList.url getter
FolderList.prototype.__defineGetter__("url", function() {
	return  "/api/workspaces/" + app.workspaceId + "/folders/" + this.parent.get("id") + "/folders";
}); 

RootFolderList = FolderList.extend({});
// RootFolderList.url getter
RootFolderList.prototype.__defineGetter__("url", function() {
	return  "/api/workspaces/" + app.workspaceId + "/folders/";
}); 

