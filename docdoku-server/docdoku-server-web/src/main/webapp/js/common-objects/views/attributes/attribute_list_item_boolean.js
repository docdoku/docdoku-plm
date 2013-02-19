define([
	"common-objects/views/attributes/attribute_list_item",
	"text!common-objects/templates/attributes/attribute_list_item.html",
	"text!common-objects/templates/attributes/attribute_list_item_boolean.html"
], function (
	AttributeListItemView,
	attribute_list_item,
	template
) {
	var AttributeListItemBooleanView = AttributeListItemView.extend({
		template: Mustache.compile(template),
		partials: {
			attribute_list_item: attribute_list_item
		},
		initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
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
	return AttributeListItemBooleanView;
});
