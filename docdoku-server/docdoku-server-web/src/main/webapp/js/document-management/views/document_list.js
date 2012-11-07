define([
	"views/checkbox_list",
	"views/document_list_item",
	"text!templates/document_list.html"
], function (
	CheckboxListView,
	DocumentListItemView,
	template
) {
	var DocumentListView = CheckboxListView.extend({
		template: Mustache.compile(template),
		itemViewFactory: function (model) {
			return new DocumentListItemView({
				model: model
			});
		}
	});
	return DocumentListView;
});
