/*global _,define*/
define([
    'backbone',
    'models/product_instance_path_data_iteration'
], function (Backbone, ProductInstanceIteration) {
    'use strict';
    var ProductInstanceIterationPathList = Backbone.Collection.extend({

        model: ProductInstanceIteration,

        className: 'ProductInstanceIterationPathList',

        initialize: function () {
        },


        setProductInstance: function (productInstance) {
            this.ProductInstanceIteration = productInstance;
        },

        url: function () {
            return this.ProductInstanceIteration.url() + '/iterations';
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

    return ProductInstanceIterationPathList;
});
