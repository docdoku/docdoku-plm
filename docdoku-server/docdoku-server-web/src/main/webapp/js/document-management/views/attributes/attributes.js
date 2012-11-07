define([
    "views/components/editable_list_view",
    "views/attributes/attribute_item_boolean",
    "views/attributes/attribute_item_date",
    "views/attributes/attribute_item_number",
    "views/attributes/attribute_item_text",
    "views/attributes/attribute_item_url"
], function (
    EditableListView,
    AttributeItemBooleanView,
    AttributeItemDateView,
    AttributeItemNumberView,
    AttributeItemTextView,
    AttributeItemUrlView
    ) {
    var AttributeListView = EditableListView.extend({
        typeViewMapping: {
            "BOOLEAN":	AttributeItemBooleanView,
            "DATE":		AttributeItemDateView,
            "NUMBER":	AttributeItemNumberView,
            "TEXT":		AttributeItemTextView,
            "URL":		AttributeItemUrlView
        },
        itemViewFactory: function (model) {
            var type = model.get("type");
            var constructor = this.typeViewMapping[type];
            return new constructor({
                model: model
            });
        },
        collectionAdd: function (model) {
            this.createItemView(model);
        }
    });
    return AttributeListView;
});
