define(function () {
	var Folder = Backbone.Model.extend({

        initialize : function(){
            this.className = "Folder";
        },

        getPath: function() {
            return this.get("path");
        },

        getName: function() {
            return this.get("name");
        },

		defaults: {
			home: false
		},
		url: function() {
			if (this.get("id")) {
				return "/api/workspaces/" + APP_CONFIG.workspaceId + "/folders/" + this.get("id");
			} else if (this.collection) {
				return this.collection.url;
			}
		}
	});
	return Folder;
});
