define([
	"views/document/document_attribute_list_item",
	"text!templates/partials/document_attribute_list_item.html",
	"text!templates/document/document_attribute_list_item_text.html"
], function (
	DocumentAttributeListItemView,
	document_attribute_list_item,
	template
) {
	var DocumentAttributeListItemTextView = DocumentAttributeListItemView.extend({
		template: Mustache.compile(template),
		partials: {
			document_attribute_list_item: document_attribute_list_item
		},
		initialize: function () {
            DocumentAttributeListItemView.prototype.initialize.apply(this, arguments);
		}
	});
	return DocumentAttributeListItemTextView;
});
