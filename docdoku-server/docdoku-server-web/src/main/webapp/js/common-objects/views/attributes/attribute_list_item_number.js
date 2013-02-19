define([
	"common-objects/views/attributes/attribute_list_item",
	"text!common-objects/templates/attributes/attribute_list_item.html",
	"text!common-objects/templates/attributes/attribute_list_item_number.html"
], function (
	AttributeListItemView,
	attribute_list_item,
	template
) {
	var AttributeListItemNumberView = AttributeListItemView.extend({
		template: Mustache.compile(template),
		partials: {
			attribute_list_item: attribute_list_item
		},
		initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
		}
	});
	return AttributeListItemNumberView;
});
