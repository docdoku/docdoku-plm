/*global $,define,App*/
define([
    'common-objects/collections/linked/linked_change_item_collection',
    'common-objects/views/linked/linked_change_items'
], function (LinkedChangeItemCollection, LinkedItemsView) {
    'use strict';
	var LinkedIssuesView = LinkedItemsView.extend({

        initialize: function () {
            LinkedItemsView.prototype.initialize.apply(this, arguments);
            this.options.label = App.config.i18n.ADD_ISSUE;
        },

        bindTypeahead: function () {
            var self = this;

            this.referenceInput.typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/issues/link/?q=' + query, function (data) {

                        self.searchResults = new LinkedChangeItemCollection(data);
                        process(self.searchResults.map(function (issue) {
                            return issue.getName();
                        }));
                    });
                },

                sorter: function (issuesNames) {
                    return issuesNames.sort();
                },

                updater: function (issueName) {
                    var linkedIssue = self.searchResults.find(function (issue) {
                        return issue.getName() === issueName;
                    });
                    linkedIssue.collection.remove(linkedIssue);
                    self.collection.add(linkedIssue);

                    self.addLinkView(linkedIssue);
                }
            });
        }

    });
    return LinkedIssuesView;
});