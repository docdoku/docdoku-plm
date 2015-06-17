/*global define*/
define([
    'common-objects/views/attributes/attribute_list_item',
    'text!common-objects/templates/attributes/attribute_list_item.html',
    'text!common-objects/templates/attributes/attribute_list_item_number.html'
], function (AttributeListItemView, attributeListItem, template) {
    'use strict';
    var AttributeListItemNumberView = AttributeListItemView.extend({

        template: template,

        partials: {
            attributeListItem: attributeListItem
        },
        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
        }
    });
    return AttributeListItemNumberView;
});
