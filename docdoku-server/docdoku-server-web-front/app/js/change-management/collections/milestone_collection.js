/*global define,App*/
define([
    'backbone',
    'models/milestone'
], function (Backbone,MilestoneModel) {
	'use strict';
    var MilestoneListCollection = Backbone.Collection.extend({
        model: MilestoneModel,
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/milestones';
        }
    });

    return MilestoneListCollection;
});