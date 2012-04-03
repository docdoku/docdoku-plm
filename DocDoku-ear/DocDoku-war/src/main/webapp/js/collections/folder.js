var FolderList = Backbone.Collection.extend({
	model: Folder,
	parse: function(data) {
		if (!this.parent) {
			data.unshift({
				id: app.workspaceId + ":~" + app.login,
				name: "~" + app.login,
				path: app.workspaceId,
				home: true
			});
		}
		return data; 
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
FolderList.prototype.__defineGetter__("url", function () {
	var baseUrl = "/api/workspaces/" + app.workspaceId + "/folders"
	if (this.parent) {
		return baseUrl + "/" + this.parent.get("id") + "/folders";
	} else {
		return  baseUrl;
	}
});
