define([
    "models/document"
], function(Document) {
    var FolderDocumentList = Backbone.Collection.extend({

        model: Document,

        url: function() {
            var baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId;
            if (this.parent) {
                return  baseUrl + "/folders" + "/" + this.parent.id + "/documents";
            } else {
                return baseUrl + "/documents";
            }
        },

        comparator: function(document) {
            return document.get("id");
        }

    });
    FolderDocumentList.className = "FolderDocumentList";
    return FolderDocumentList;
});
