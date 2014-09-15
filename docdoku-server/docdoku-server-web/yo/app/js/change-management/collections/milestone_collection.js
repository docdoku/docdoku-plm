define([
    "backbone",
    "models/milestone"
], function (Backbone,MilestoneModel) {
    var MilestoneListCollection = Backbone.Collection.extend({
        model: MilestoneModel,
        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/milestones";
        }
    });

    return MilestoneListCollection;
});