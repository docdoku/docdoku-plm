define([
    "models/change_request"
], function (
    ChangeRequestModel
    ) {
    var ChangeRequestListCollection = Backbone.Collection.extend({
        model: ChangeRequestModel,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/requests"
    });

    return ChangeRequestListCollection;
});