/*global _,define*/
define([
    'backbone',
    'common-objects/models/path_data_iteration'
], function (Backbone, PathDataIteration) {
    'use strict';
    var PathDataIterations = Backbone.Collection.extend({

        model: PathDataIteration,

        className: 'PathDataIterations',

        initialize: function () {
        },

        setPathDataMaster: function (pathDataMaster) {
            this.pathDataMaster = pathDataMaster;
        },

        url: function () {
            return this.pathDataMaster.url() + '/iterations';
        },

        next: function (iteration) {
            var index = this.indexOf(iteration);
            return this.at(index + 1);
        },

        previous: function (iteration) {
            var index = this.indexOf(iteration);
            return this.at(index - 1);
        },

        hasNextIteration: function (iteration) {
            return !_.isUndefined(this.next(iteration));
        },

        hasPreviousIteration: function (iteration) {
            return !_.isUndefined(this.previous(iteration));
        },

        isLast: function (iteration) {
            return this.last() === iteration;
        }


    });

    return PathDataIterations;

});
