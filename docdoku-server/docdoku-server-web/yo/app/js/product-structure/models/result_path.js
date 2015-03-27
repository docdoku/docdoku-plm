/*global define,_*/
'use strict';
define(['backbone'], function (Backbone) {

    var ResultPath = Backbone.Model.extend({

        contains: function (partUsageLinkId) {
            return _.indexOf(this.partUsageLinks, partUsageLinkId) !== -1;
        },

        parse: function (response) {
            if (response) {
                var linkIds = response.path.substr(3).split('-');
                linkIds.unshift('-1');
                this.partUsageLinks = linkIds;
            }
        },

        toJSON: function () {
            return _.pick(this.attributes, 'path');
        }

    });

    return ResultPath;

});
