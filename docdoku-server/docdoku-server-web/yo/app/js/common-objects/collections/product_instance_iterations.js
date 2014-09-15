/*global define*/
define(['backbone', "common-objects/models/product_instance_iteration"], function (Backbone, ProductInstanceIteration) {
    var ProductInstanceIterations = Backbone.Collection.extend({
        model: ProductInstanceIteration,

        setMaster: function (master) {
            this.master = master;
        },

        url: function () {
            return this.master.url() + "/iterations";
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
            return this.last() == iteration;
        }

    });
    return ProductInstanceIterations;
});