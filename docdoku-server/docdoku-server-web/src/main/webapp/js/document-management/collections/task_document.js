define([
	"models/document"
], function (
	Document
) {
	var TaskDocumentList = Backbone.Collection.extend({

		model: Document,

        className:"TaskDocumentList",

        url: function() {
            baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/tasks"
            return baseUrl + "/"+  APP_CONFIG.login +"/documents/";
        }

	});

	return TaskDocumentList;
});
