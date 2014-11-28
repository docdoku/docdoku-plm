/*global define*/
define([
    'backbone',
    'common-objects/models/workflow/activity_model'
], function (Backbone, ActivityModel) {
	'use strict';
    var ActivityModels = Backbone.Collection.extend({
        model: ActivityModel
    });

    return ActivityModels;
});
