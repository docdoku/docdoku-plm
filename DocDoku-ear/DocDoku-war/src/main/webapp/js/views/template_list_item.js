define([
	"i18n",
	"common/date",
	"views/checkbox_list_item",
	"text!templates/template_list_item.html"
], function (
	i18n,
	date,
	CheckboxListItemView,
	template
) {
	var TemplateListItemView = CheckboxListItemView.extend({
		template: Mustache.compile(template),
		tagName: "tr",
		modelToJSON: function () {
			var data = this.model.toJSON();
			// Format dates
			data.creationDate = date.formatTimestamp(
				i18n._DATE_FORMAT,
				data.creationDate);
			return data;
		},
	});
	return TemplateListItemView;
});
