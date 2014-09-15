/*global define*/
define([
    "models/document"
], function (Document) {
    var TaskDocumentList = Backbone.Collection.extend({

        model: Document,

        className: "TaskDocumentList",

        setFilterStatus: function (status) {
            this.filterStatus = status;
        },

        url: function () {
            var url = APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/tasks/" + APP_CONFIG.login + "/documents/";
            if (this.filterStatus) {
                url += "?filter=" + this.filterStatus;
            }
            return url;
        },

        comparator: function (document) {
            return document.get("id");
        }

    });

    return TaskDocumentList;
});
