define([
	"views/attributes/attribute_item",
	"text!templates/partials/document_new_attribute_list_item.html",
	"text!templates/document_new/document_new_attribute_list_item_boolean.html"
], function (
	AttributeItemView,
    attribute_partial,
	template
) {
	var AttributeItemBooleanView = AttributeItemView.extend({
		template: Mustache.compile(template),
		partials: {
			document_new_attribute_list_item: attribute_partial
		},
		initialize: function () {
            AttributeItemView.prototype.initialize.apply(this, arguments);
			this.events["change .value"] = "updateValue";
		},
		getValue: function (el) {
			return el.attr("checked") ? true : false;
		}
	});
	return AttributeItemBooleanView;
});
