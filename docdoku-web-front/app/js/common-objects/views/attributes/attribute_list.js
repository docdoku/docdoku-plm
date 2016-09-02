/*global define*/
define([
    'common-objects/views/components/list',
    'common-objects/views/attributes/attribute_list_item_boolean',
    'common-objects/views/attributes/attribute_list_item_date',
    'common-objects/views/attributes/attribute_list_item_number',
    'common-objects/views/attributes/attribute_list_item_text',
    'common-objects/views/attributes/attribute_list_item_long_text',
    'common-objects/views/attributes/attribute_list_item_url',
    'common-objects/views/attributes/attribute_list_item_lov'
], function (ListView, AttributeListItemBooleanView, AttributeListItemDateView, AttributeListItemNumberView, AttributeListItemTextView, AttributeListItemLongTextView, AttributeListItemUrlView, AttributeListItemLOVView) {
    'use strict';
    var AttributeListView = ListView.extend({

        typeViewMapping: {
            'BOOLEAN': AttributeListItemBooleanView,
            'DATE': AttributeListItemDateView,
            'NUMBER': AttributeListItemNumberView,
            'TEXT': AttributeListItemTextView,
            'LONG_TEXT': AttributeListItemLongTextView,
            'URL': AttributeListItemUrlView,
            'LOV': AttributeListItemLOVView
        },

        editMode: true,

        attributesLocked: false,


        initialize: function () {
            ListView.prototype.initialize.apply(this, arguments);
            this.lovs = this.options.lovs;
            this.displayOnly = this.options.displayOnly ? this.options.displayOnly : false;
        },

        itemViewFactory: function (model) {
            var type = model.get('type');
            if (!type) {
                type = model.get('attributeType');
            }

            if(type !== 'TEXT' && type !== 'LONG_TEXT' && type !== 'BOOLEAN' && type !== 'NUMBER' && type !== 'URL' && type !== 'DATE'){
                type = 'LOV';
            }
            var Constructor = this.typeViewMapping[type];
            var view = new Constructor({
                model: model,
                lovs:this.lovs.models,
                displayOnly: this.displayOnly
            });
            view.setEditMode(this.editMode);
            view.setAttributesLocked(this.attributesLocked);
            return view;
        },

        collectionAdd: function (model) {
            this.createItemView(model);
        },


        setEditMode: function (editMode) {
            this.editMode = editMode;
        },

        setAttributesLocked: function (attributesLocked) {
            this.attributesLocked = attributesLocked;
        }

    });
    return AttributeListView;
});
