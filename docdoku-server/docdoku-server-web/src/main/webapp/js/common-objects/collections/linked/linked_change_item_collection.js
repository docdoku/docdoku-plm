define([
    "common-objects/models/linked/linked_change_item"
], function (
    LinkedChangeItem
    ) {
    var LinkedChangeItemCollection = Backbone.Collection.extend({

        model: LinkedChangeItem,

        comparator: function(linkedChangeItem) {
            return linkedChangeItem.getName();
        }

    });

    return LinkedChangeItemCollection;
});
