/*global define*/
define([
    'backbone',
    "common-objects/models/workflow_model"
], function (Backbone, WorkflowModel) {
    var WorkflowModels = Backbone.Collection.extend({
        model: WorkflowModel,
        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/workflows";
        }
    });

    return WorkflowModels;
});
