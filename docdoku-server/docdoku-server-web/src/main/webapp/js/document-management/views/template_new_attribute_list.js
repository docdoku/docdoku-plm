define([
	"views/components/list",
	"views/template_new_attribute_list_item"
], function (
	ListView,
	TemplateNewAttributeListItemView
) {
	var TemplateNewAttributeListView = ListView.extend({
		itemViewFactory: function (model) {
			return new TemplateNewAttributeListItemView({
				model: model
			});
		}
	});
	return TemplateNewAttributeListView;
});
