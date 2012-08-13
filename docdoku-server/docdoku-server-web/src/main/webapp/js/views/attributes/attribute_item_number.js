define([
	"views/attributes/attribute_item",
	"text!templates/partials/document_new_attribute_list_item.html",
	"text!templates/document_new/document_new_attribute_list_item_number.html"
], function (
	AttributeItemView,
	partial,
	template
) {
	var AttributeItemNumberView = AttributeItemView.extend({
		template: Mustache.compile(template),
		partials: {
			document_new_attribute_list_item: partial
		},
		initialize: function () {
            AttributeItemView.prototype.initialize.apply(this, arguments);
		}
	});
	return AttributeItemNumberView;
});
