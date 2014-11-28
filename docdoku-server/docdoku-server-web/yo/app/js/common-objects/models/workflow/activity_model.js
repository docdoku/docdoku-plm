/*global _,define*/
define([
    'backbone',
    'common-objects/collections/task_models'
], function (Backbone, TaskModels) {
	'use strict';
    var ActivityModel = Backbone.Model.extend({

        defaults: function () {
            return {
                type: 'SERIAL',
                tasksToComplete: 0,
                taskModels: new TaskModels()
            };
        },

        initialize: function () {
            if (_.isUndefined(this.get('taskModels').models)) {
                this.set({
                    taskModels: new TaskModels(this.get('taskModels'))
                });
            }
        },

        toJSON: function () {
            var index = this.collection.indexOf(this);
            _.extend(this.attributes, {step: index});

            return _.clone(this.attributes);
        }

    });

    return ActivityModel;
});
