define([
	"common-objects/views/components/list",
	"common-objects/views/attributes/template_new_attribute_list_item"
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
