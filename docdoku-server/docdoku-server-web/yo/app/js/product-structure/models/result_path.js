/*global define*/
'use strict';
define(['backbone'], function (Backbone) {

    var ResultPath = Backbone.Model.extend({

        contains: function (partUsageLinkId) {
            return _.indexOf(this.partUsageLinks, partUsageLinkId) !== -1;
        },

        parse: function (response) {
            if (response) {
                this.partUsageLinks = _.map(response.path.split('-'), function (partUsageLinkIdString) {
                    return parseInt(partUsageLinkIdString, 10);
                });
                this.partUsageLinks.unshift(-1);
            }
        },

        toJSON: function () {
            return _.pick(this.attributes, 'path');
        }

    });

    return ResultPath;

});
