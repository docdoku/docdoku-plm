define([
    "models/milestone"
], function (
    MilestoneModel
    ) {
    var MilestoneListCollection = Backbone.Collection.extend({
        model: MilestoneModel,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/milestones"
    });

    return MilestoneListCollection;
});