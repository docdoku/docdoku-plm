define([
    "models/change_issue"
], function (
    ChangeIssueModel
    ) {
    var ChangeIssueListCollection = Backbone.Collection.extend({
        model: ChangeIssueModel,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/issues"
    });

    return ChangeIssueListCollection;
});