var FolderList = Backbone.Collection.extend({
	model: Folder,
	parse: function(data) {
		// adding folders' id from completePath
		// done here because completePath is not reachable in initialize
		var folders = [];
		console.debug(this.url)
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
	}
});
