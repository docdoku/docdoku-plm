var FolderList = Backbone.Collection.extend({
	model: Folder,
	parse: function(data) {
		// adding folders' id from completePath
		// done here because completePath is not reachable in initialize
		var folders = [];
		if (this.home != undefined) {
			data.unshift({
				completePath: this.home,
				home: true
			});
		}
		_.each(data, function (item) {
			name = app.basename(item.completePath);
			folders.push({
				id: name,
				name: name,
				completePath: item.completePath,
				home: item.home ? item.home : false
			});	    		
		});
		return folders; 
	}
});
