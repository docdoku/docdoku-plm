define([
    "models/part_iteration"
], function (
    PartIteration
    ) {
    var PartIterationList = Backbone.Collection.extend({

        model: PartIteration,

        setPart: function(part) {
            this.part = part;
        },

        url: function() {
            return this.part.url() + "/iterations";
        },

        next: function(iteration) {
            var index = this.indexOf(iteration);
            return this.at(index + 1);
        },

        previous: function(iteration) {
            var index = this.indexOf(iteration);
            return this.at(index - 1);
        },

        hasNextIteration: function(iteration) {
            return !_.isUndefined(this.next(iteration));
        },

        hasPreviousIteration: function(iteration) {
            return !_.isUndefined(this.previous(iteration));
        },

        isLast: function(iteration) {
            return this.last() == iteration;
        }

    });

    return PartIterationList;
});
