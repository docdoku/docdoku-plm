var FolderList = Backbone.Collection.extend({
	model: Folder,
	parse: function(data) {
		// adding folders' id from completePath
		// done here because completePath is not reachable in initialize
		var folders = [];
		_.each(data, function (item) {
			item.id = dirname(item.completePath)
			folders.push(item);	    		
		});
		return folders;        			   	
	}
});
