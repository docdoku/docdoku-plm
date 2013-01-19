define([
	"views/components/list",
	"views/document/document_attribute_list_item_boolean",
	"views/document/document_attribute_list_item_date",
	"views/document/document_attribute_list_item_number",
	"views/document/document_attribute_list_item_text",
	"views/document/document_attribute_list_item_url"
], function (
	ListView,
	DocumentAttributeListItemBooleanView,
	DocumentAttributeListItemDateView,
	DocumentAttributeListItemNumberView,
	DocumentAttributeListItemTextView,
	DocumentAttributeListItemUrlView
) {
	var DocumentAttributeListView = ListView.extend({

        typeViewMapping: {
				"BOOLEAN":	DocumentAttributeListItemBooleanView,
				"DATE":		DocumentAttributeListItemDateView,
				"NUMBER":	DocumentAttributeListItemNumberView,
				"TEXT":		DocumentAttributeListItemTextView,
				"URL":		DocumentAttributeListItemUrlView
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
	return DocumentAttributeListView;
});
