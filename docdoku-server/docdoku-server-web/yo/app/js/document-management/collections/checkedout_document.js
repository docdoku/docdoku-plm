/*global define*/
define([
    "backbone",
    "models/document"
], function (Backbone,Document) {
    var CheckedoutDocumentList = Backbone.Collection.extend({

        model: Document,

        className: "CheckedoutDocumentList",

        url: function () {
            return  APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/documents/checkedout";
        }

    });

    return CheckedoutDocumentList;
});
