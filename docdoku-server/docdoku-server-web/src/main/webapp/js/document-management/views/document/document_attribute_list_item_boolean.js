define([
	"views/document/document_attribute_list_item",
	"text!templates/partials/document_attribute_list_item.html",
	"text!templates/document/document_attribute_list_item_boolean.html"
], function (
	DocumentAttributeListItemView,
	document_attribute_list_item,
	template
) {
	var DocumentAttributeListItemBooleanView = DocumentAttributeListItemView.extend({
		template: Mustache.compile(template),
		partials: {
			document_attribute_list_item: document_attribute_list_item
		},
		initialize: function () {
            DocumentAttributeListItemView.prototype.initialize.apply(this, arguments);
			this.events["change .value"] = "updateValue";
		},
		getValue: function (el) {
			return el.is(':checked');
		},
        modelToJSON: function() {
            return {
                name: this.model.get('name'),
                type: this.model.get('type'),
                value: this.model.get('value') == "true"
            };
        }
	});
	return DocumentAttributeListItemBooleanView;
});
