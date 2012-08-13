define([
	"i18n",
	"views/attributes/attribute_item",
	"text!templates/partials/document_new_attribute_list_item.html",
	"text!templates/document_new/document_new_attribute_list_item_date.html"
], function (
	i18n,
	AttributeItemView,
    attribute_partial,
	template
) {
	var AttributeItemDateView = AttributeItemView.extend({
		template: Mustache.compile(template),
		partials: {
			document_new_attribute_list_item: attribute_partial
		},
		initialize: function () {
            AttributeItemView.prototype.initialize.apply(this, arguments);
		},
		modelToJSON: function () {
			var data = this.model.toJSON();
			if (data.value) {
				data.value = $.datepicker.formatDate(
					i18n["_DATE_PICKER_DATE_FORMAT"],
					new Date(data.value)
				);
			}
			return data;
		},
		rendered: function () {
            AttributeItemView.prototype.rendered.apply(this, arguments);
			this.$el.find("input.value:first").datepicker({
				dateFormat: i18n["_DATE_PICKER_DATE_FORMAT"]
			});
		},
		getValue: function (el) {
			var value = null;
			if (el.val()) {
				value = $.datepicker.parseDate(
					i18n["_DATE_PICKER_DATE_FORMAT"],
					el.val()
				).getTime();
			};
			return value;
		}
	});
	return AttributeItemDateView;
});
