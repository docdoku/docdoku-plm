/*global define,App*/
define([
    'backbone',
    'common-objects/collections/activity_models'
], function (Backbone, ActivityModels) {
	'use strict';
    var WorkflowModel = Backbone.Model.extend({

        urlRoot: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/workflows';
        },

        defaults: function () {
            return {
                activityModels: new ActivityModels()
            };
        },

        parse: function (response) {
            response.activityModels = new ActivityModels(response.activityModels);
            return response;
        }

    });
    return WorkflowModel;
});
