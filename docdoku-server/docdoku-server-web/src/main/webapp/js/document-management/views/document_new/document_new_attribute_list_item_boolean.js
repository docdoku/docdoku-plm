define([
	"views/document_new/document_new_attribute_list_item",
	"text!templates/partials/document_new_attribute_list_item.html",
	"text!templates/document_new/document_new_attribute_list_item_boolean.html"
], function (
	DocumentNewAttributeListItemView,
	document_new_attribute_list_item,
	template
) {
	var DocumentNewAttributeListItemBooleanView = DocumentNewAttributeListItemView.extend({
		template: Mustache.compile(template),
		partials: {
			document_new_attribute_list_item: document_new_attribute_list_item
		},
		initialize: function () {
			DocumentNewAttributeListItemView.prototype.initialize.apply(this, arguments);
			this.events["change .value"] = "updateValue";
		},
		getValue: function (el) {
			return el.attr("checked") ? true : false;
		}
	});
	return DocumentNewAttributeListItemBooleanView;
});
