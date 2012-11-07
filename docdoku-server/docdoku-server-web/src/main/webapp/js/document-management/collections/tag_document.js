define([
	"models/document"
], function (
	Document
) {
	var TagDocumentList = Backbone.Collection.extend({
		model: Document,
	});
	TagDocumentList.prototype.__defineGetter__("url", function () {
		baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/tags"
		return baseUrl + "/" + this.parent.get("label") + "/documents";
	});
    TagDocumentList.className="TagDocumentList";
	return TagDocumentList;
});
