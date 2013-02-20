define([
	"models/document"
], function (
	Document
) {
	var TagDocumentList = Backbone.Collection.extend({

		model: Document,

        className:"TagDocumentList",

        url: function() {
            var baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/tags";
            return baseUrl + "/" + this.parent.get("label") + "/documents";
        }

	});

	return TagDocumentList;
});
