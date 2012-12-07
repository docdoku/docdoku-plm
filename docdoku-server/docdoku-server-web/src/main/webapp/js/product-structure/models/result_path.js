define(function () {

    var ResultPath = Backbone.Model.extend({

        contains: function(partUsageLinkId) {
            return _.indexOf(this.partUsageLinks, partUsageLinkId);
        },

        parse: function(response) {
            if (response) {
                this.partUsageLinks = _.map(response.split('-'), function(partUsageLinkIdString) {
                    return parseInt(partUsageLinkIdString, 10);
                });
            }
        },

        toJSON: function() {
            return _.pick(this.attributes, 'path');
        }

    });

    return ResultPath;

});
