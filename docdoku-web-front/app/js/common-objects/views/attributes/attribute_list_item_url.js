/*global define*/
define([
    'common-objects/views/attributes/attribute_list_item',
    'text!common-objects/templates/attributes/attribute_list_item.html',
    'text!common-objects/templates/attributes/attribute_list_item_url.html'
], function (AttributeListItemView, attributeListItem, template) {
    'use strict';
    var AttributeListItemUrlView = AttributeListItemView.extend({

        template: template,
        partials: {
            attributeListItem: attributeListItem
        },
        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
        }
    });
    return AttributeListItemUrlView;
});
