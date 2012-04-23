var DocumentNewAttributeListItemDateView = DocumentNewAttributeListItemView.extend({
	template: "document-new-attribute-list-item-date-tpl",
	initialize: function () {
		DocumentNewAttributeListItemView.prototype.initialize.apply(this, arguments);
	},
	modelToJSON: function () {
		var data = this.model.toJSON();
		if (data.value) {
			data.value = $.datepicker.formatDate(
				app.i18n["_DATE_PICKER_DATE_FORMAT"],
				new Date(data.value)
			);
		}
		return data;
	},
	rendered: function () {
		DocumentNewAttributeListItemView.prototype.rendered.apply(this, arguments);
		this.$el.find("input.value:first").datepicker({
			dateFormat: app.i18n["_DATE_PICKER_DATE_FORMAT"]
		});
	},
	getValue: function (el) {
		var value = null;
		if (el.val()) {
			value = $.datepicker.parseDate(
				app.i18n["_DATE_PICKER_DATE_FORMAT"],
				el.val()
			).getTime();
		};
		return value;
	},
});
