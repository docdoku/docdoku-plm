define([
	"models/document"
], function (
	Document
) {
	var FolderDocumentList = Backbone.Collection.extend({
		model: Document
	});
	FolderDocumentList.prototype.__defineGetter__("url", function () {
		var baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId;
		if (this.parent) {
			return  baseUrl + "/folders" + "/" + this.parent.id + "/documents";
		} else {
			return baseUrl + "/documents";
		}
	});
    FolderDocumentList.className="FolderDocumentList";
	return FolderDocumentList;
});
