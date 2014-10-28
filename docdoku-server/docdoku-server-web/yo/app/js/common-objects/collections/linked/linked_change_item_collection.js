/*global define*/
define([
    'backbone',
    'common-objects/models/linked/linked_change_item'
], function (Backbone, LinkedChangeItem) {
	'use strict';
    var LinkedChangeItemCollection = Backbone.Collection.extend({

        model: LinkedChangeItem,

        comparator: function (linkedChangeItem) {
            return linkedChangeItem.getName();
        }

    });

    return LinkedChangeItemCollection;
});
