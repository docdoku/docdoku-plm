define([
	"common-objects/views/components/list",
	"common-objects/views/attributes/attribute_list_item_boolean",
	"common-objects/views/attributes/attribute_list_item_date",
	"common-objects/views/attributes/attribute_list_item_number",
	"common-objects/views/attributes/attribute_list_item_text",
	"common-objects/views/attributes/attribute_list_item_url"
], function (
	ListView,
	AttributeListItemBooleanView,
	AttributeListItemDateView,
	AttributeListItemNumberView,
	AttributeListItemTextView,
	AttributeListItemUrlView
) {
	var AttributeListView = ListView.extend({

        typeViewMapping: {
				"BOOLEAN":	AttributeListItemBooleanView,
				"DATE":		AttributeListItemDateView,
				"NUMBER":	AttributeListItemNumberView,
				"TEXT":		AttributeListItemTextView,
				"URL":		AttributeListItemUrlView
		},

        editMode: true,

		itemViewFactory: function (model) {
			var type = model.get("type");
			var constructor = this.typeViewMapping[type];
            var view = new constructor({
                model: model
            });
            view.setEditMode(this.editMode);
			return view;
		},

		collectionAdd: function (model) {
			this.createItemView(model);
		},

        setEditMode: function(editMode) {
            this.editMode = editMode;
        }

	});
	return AttributeListView;
});
