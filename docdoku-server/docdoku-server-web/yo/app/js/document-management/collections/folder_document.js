/*global define*/
define([
    "backbone",
    "models/document"
], function (Backbone, Document) {
    var FolderDocumentList = Backbone.Collection.extend({

        model: Document,

        url: function () {
            var baseUrl = APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId;
            if (this.parent) {
                return  baseUrl + "/folders" + "/" + this.parent.id + "/documents";
            } else {
                return  baseUrl + "/folders" + "/" + APP_CONFIG.workspaceId + "/documents";
            }
        },

        comparator: function (document) {
            return document.get("id");
        }

    });
    FolderDocumentList.className = "FolderDocumentList";
    return FolderDocumentList;
});
