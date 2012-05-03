define([
	"i18n",
	"common/date",
	"views/checkbox_list_item",
	"text!templates/document_list_item.html"
], function (
	i18n,
	date,
	CheckboxListItemView,
	template
) {
	var DocumentListItemView = CheckboxListItemView.extend({
		template: Mustache.compile(template),
		tagName: "tr",
		modelToJSON: function () {
			var data = this.model.toJSON();

			// Format dates
			if (data.lastIteration && data.lastIteration.creationDate) {
				data.lastIteration.creationDate = date.formatTimestamp(
					i18n._DATE_FORMAT,
					data.lastIteration.creationDate);
			}
			if (data.checkOutDate) {
				data.checkOutDate = date.formatTimestamp(
					i18n._DATE_FORMAT,
					data.checkOutDate);
			}
			return data;
		},
	});
	return DocumentListItemView;
});
