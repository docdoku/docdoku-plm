define([
    "models/change_item"
], function (ChangeItemModel) {
    var ChangeIssueModel = ChangeItemModel.extend({
        urlRoot: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/issues";
        },
        getInitiator: function () {
            return this.get("initiator");
        }
    });

    return ChangeIssueModel;
});