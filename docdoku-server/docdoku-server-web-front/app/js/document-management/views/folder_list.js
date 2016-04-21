/*global define*/
define([
    'require',
    'common-objects/views/components/collapsible_list'
], function (require, CollapsibleListView) {
	'use strict';
    var FolderListView = CollapsibleListView.extend({
        itemViewFactory: function (model) {
            var FolderListItemView = require('views/folder_list_item'); // Circular dependency
            return new FolderListItemView({
                model: model
            });
        }
    });
    return FolderListView;
});
