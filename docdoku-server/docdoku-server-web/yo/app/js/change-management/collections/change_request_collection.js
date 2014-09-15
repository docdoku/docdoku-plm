define([
    "backbone",
    "models/change_request"
], function (Backbone,ChangeRequestModel) {
    var ChangeRequestListCollection = Backbone.Collection.extend({
        model: ChangeRequestModel,
        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/requests";
        }
    });

    return ChangeRequestListCollection;
});