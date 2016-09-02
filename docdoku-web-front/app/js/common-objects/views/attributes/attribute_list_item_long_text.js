/*global define*/
define([
    'common-objects/views/attributes/attribute_list_item',
    'text!common-objects/templates/attributes/attribute_list_item.html',
    'text!common-objects/templates/attributes/attribute_list_item_long_text.html'
], function (AttributeListItemView, attributeListItem, template) {
    'use strict';
    var AttributeListItemLongTextView = AttributeListItemView.extend({

        template: template,
        partials: {
            attributeListItem: attributeListItem
        },
        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
        },
        updateValue: function () {
            var el = this.$el.find('textarea.value:first');
            this.model.set({
                value: this.getValue(el)
            });
        }
    });
    return AttributeListItemLongTextView;
});
