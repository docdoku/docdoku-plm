define([
	"i18n",
	"views/document_new/document_new_attribute_list_item",
	"text!templates/partials/document_new_attribute_list_item.html",
	"text!templates/document_new/document_new_attribute_list_item_date.html"
], function (
	i18n,
	DocumentNewAttributeListItemView,
	document_new_attribute_list_item,
	template
) {
	var DocumentNewAttributeListItemDateView = DocumentNewAttributeListItemView.extend({
        pickerSet : false,
		template: Mustache.compile(template),
		partials: {
			document_new_attribute_list_item: document_new_attribute_list_item
		},
		initialize: function () {
			DocumentNewAttributeListItemView.prototype.initialize.apply(this, arguments);
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
			DocumentNewAttributeListItemView.prototype.rendered.apply(this, arguments);
			if (! this.pickerSet){
                this.$el.find("input.value:first").datepicker({
                    dateFormat: i18n["_DATE_PICKER_DATE_FORMAT"]
                });
                this.pickerSet = true;
            }

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
	return DocumentNewAttributeListItemDateView;
});
