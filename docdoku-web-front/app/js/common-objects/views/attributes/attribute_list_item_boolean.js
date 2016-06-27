/*global define*/
define([
    'common-objects/views/attributes/attribute_list_item',
    'text!common-objects/templates/attributes/attribute_list_item.html',
    'text!common-objects/templates/attributes/attribute_list_item_boolean.html'
], function (AttributeListItemView, attributeListItem, template) {
    'use strict';
    var AttributeListItemBooleanView = AttributeListItemView.extend({

        template: template,

        partials: {
            attributeListItem: attributeListItem
        },
        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
            this.events['change .value'] = 'updateValue';
        },
        getValue: function (el) {
            return el.is(':checked');
        },
        modelToJSON: function () {
            return {
                name: this.model.get('name'),
                type: this.model.get('type'),
                value: this.model.get('value') === true || this.model.get('value') === 'true',
                locked: this.model.get('locked')
            };
        }
    });
    return AttributeListItemBooleanView;
});
