define([
	"models/document"
], function (
	Document
) {
	var CheckedoutDocumentList = Backbone.Collection.extend({
		model: Document,
	});
	CheckedoutDocumentList.prototype.__defineGetter__("url", function () {
		baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/documents";
		return  baseUrl + "/checkedout";
	});
    CheckedoutDocumentList.className="CheckedoutDocumentList";
	return CheckedoutDocumentList;
});
