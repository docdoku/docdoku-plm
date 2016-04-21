/*global $,define,App*/
define([
    'common-objects/collections/linked/linked_change_item_collection',
    'common-objects/views/linked/linked_change_items'
], function (LinkedChangeItemCollection, LinkedItemsView) {
	'use strict';
    var LinkedRequestsView = LinkedItemsView.extend({

        initialize: function () {
            LinkedItemsView.prototype.initialize.apply(this, arguments);
            this.options.label = App.config.i18n.ADD_REQUEST;
        },

        bindTypeahead: function () {
            var self = this;

            this.referenceInput.typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/requests/link/?q=' + query, function (data) {

                        self.searchResults = new LinkedChangeItemCollection(data);
                        process(self.searchResults.map(function (request) {
                            return request.getName();
                        }));
                    });
                },

                sorter: function (requestsNames) {
                    return requestsNames.sort();
                },

                updater: function (requestName) {
                    var linkedRequest = self.searchResults.find(function (request) {
                        return request.getName() === requestName;
                    });
                    linkedRequest.collection.remove(linkedRequest);
                    self.collection.add(linkedRequest);

                    self.addLinkView(linkedRequest);
                }
            });
        }

    });
    return LinkedRequestsView;
});