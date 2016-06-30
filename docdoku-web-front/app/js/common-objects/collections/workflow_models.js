/*global define,App*/
define([
    'backbone',
    'common-objects/models/workflow/workflow_model'
], function (Backbone, WorkflowModel) {
	'use strict';
    var WorkflowModels = Backbone.Collection.extend({
        model: WorkflowModel,
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/workflow-models';
        }
    });

    return WorkflowModels;
});
