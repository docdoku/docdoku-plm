define([
	"models/document"
], function (
	Document
) {
	var TagDocumentList = Backbone.Collection.extend({

		model: Document,

        className:"TagDocumentList",

        url: function() {
            var tagsUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/tags";
            return tagsUrl + "/" + this.parent.get("label") + "/documents";
        },

        comparator: function(document) {
            return document.get("id");
        }

	});

	return TagDocumentList;
});
