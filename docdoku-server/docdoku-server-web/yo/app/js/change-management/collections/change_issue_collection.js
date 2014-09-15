define([
    "backbone",
    "models/change_issue"
], function (Backbone,ChangeIssueModel) {
    var ChangeIssueListCollection = Backbone.Collection.extend({
        model: ChangeIssueModel,
        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/issues";
        }
    });

    return ChangeIssueListCollection;
});