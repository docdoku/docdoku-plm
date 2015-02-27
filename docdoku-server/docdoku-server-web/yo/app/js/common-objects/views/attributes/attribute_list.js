/*global define*/
define([
    "common-objects/views/components/list",
    "common-objects/views/attributes/attribute_list_item_boolean",
    "common-objects/views/attributes/attribute_list_item_date",
    "common-objects/views/attributes/attribute_list_item_number",
    "common-objects/views/attributes/attribute_list_item_text",
    "common-objects/views/attributes/attribute_list_item_url"
], function (ListView, AttributeListItemBooleanView, AttributeListItemDateView, AttributeListItemNumberView, AttributeListItemTextView, AttributeListItemUrlView) {
    var AttributeListView = ListView.extend({

        typeViewMapping: {
            "BOOLEAN": AttributeListItemBooleanView,
            "DATE": AttributeListItemDateView,
            "NUMBER": AttributeListItemNumberView,
            "TEXT": AttributeListItemTextView,
            "URL": AttributeListItemUrlView
        },

        editMode: true,

        attributesLocked: false,

        itemViewFactory: function (model) {
            var type = model.get("type");
            if (!type) {
                type = model.get("attributeType");
            }
            var constructor = this.typeViewMapping[type];
            var view = new constructor({
                model: model
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
