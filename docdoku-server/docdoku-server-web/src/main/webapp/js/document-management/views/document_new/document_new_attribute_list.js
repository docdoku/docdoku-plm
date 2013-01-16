define([
	"views/components/list",
	"views/document_new/document_new_attribute_list_item_boolean",
	"views/document_new/document_new_attribute_list_item_date",
	"views/document_new/document_new_attribute_list_item_number",
	"views/document_new/document_new_attribute_list_item_text",
	"views/document_new/document_new_attribute_list_item_url"
], function (
	ListView,
	DocumentNewAttributeListItemBooleanView,
	DocumentNewAttributeListItemDateView,
	DocumentNewAttributeListItemNumberView,
	DocumentNewAttributeListItemTextView,
	DocumentNewAttributeListItemUrlView
) {
	DocumentNewAttributeListView = ListView.extend({

        typeViewMapping: {
				"BOOLEAN":	DocumentNewAttributeListItemBooleanView,
				"DATE":		DocumentNewAttributeListItemDateView,
				"NUMBER":	DocumentNewAttributeListItemNumberView,
				"TEXT":		DocumentNewAttributeListItemTextView,
				"URL":		DocumentNewAttributeListItemUrlView
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
	return DocumentNewAttributeListView;
});
