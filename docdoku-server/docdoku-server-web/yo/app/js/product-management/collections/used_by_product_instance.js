/*global define,App*/
define([
    'backbone',
    'common-objects/models/product_instance'
], function (Backbone, ProductInstance) {
    'use strict';
    var UsedByProductInstanceList = Backbone.Collection.extend({

        model: ProductInstance,

        setLinkedPart: function (linkedPart) {
            this.linkedPart = linkedPart;
        },

        url: function () {
            return this.linkedPart.url() + '/used-by-productInstanceMasters';
        }

    });

    UsedByProductInstanceList.className = 'UsedByProductInstanceList';

    return UsedByProductInstanceList;
});
