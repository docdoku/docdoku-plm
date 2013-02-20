define([
	"views/checkbox_list",
	"views/template_list_item",
	"text!templates/template_list.html"
], function (
	CheckboxListView,
	TemplateListItemView,
	template
) {
	var TemplateListView = CheckboxListView.extend({
		template: Mustache.compile(template),
		itemViewFactory: function (model) {
			return new TemplateListItemView({
				model: model
			});
		}
	});
	return TemplateListView;
});
