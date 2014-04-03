define([
    "common-objects/collections/linked/linked_change_item_collection",
    "common-objects/views/linked/linked_change_items",
    "i18n!localization/nls/change-management-strings"
], function(LinkedChangeItemCollection, LinkedItemsView, i18n) {
    var LinkedOrdersView = LinkedItemsView.extend({

        initialize: function() {
            LinkedItemsView.prototype.initialize.apply(this, arguments);
            this.options.label = i18n.ADD_REQUEST;
        },

        bindTypeahead: function() {
            var self = this;

            this.referenceInput.typeahead({
                source: function(query, process) {
                    $.getJSON('/api/workspaces/' + APP_CONFIG.workspaceId + '/changes/orders/link/?q=' + query, function(data) {

                        self.searchResults = new LinkedChangeItemCollection(data);
                        process(self.searchResults.map(function(order) {
                            return order.getName();
                        }));
                    });
                },

                sorter: function(ordersNames) {
                    return ordersNames.sort();
                },

                updater: function(orderName) {
                    var linkedOrder = self.searchResults.find(function(order) {
                        return order.getName() == orderName;
                    });
                    linkedOrder.collection.remove(linkedOrder);
                    self.collection.add(linkedOrder);

                    self.addLinkView(linkedOrder);
                }
            });
        }

    });
    return LinkedOrdersView;
});