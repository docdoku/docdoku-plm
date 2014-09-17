/*global define*/
define([
    'backbone',
    'common-objects/models/task_model'
], function (Backbone, TaskModel) {
	'use strict';
    var TaskModels = Backbone.Collection.extend({
        model: TaskModel
    });

    return TaskModels;
});