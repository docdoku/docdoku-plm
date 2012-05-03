define([
	"views/list",
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
		},
		collectionAdd: function (model) {
			this.createItemView(model);
		},
	});
	return TemplateNewAttributeListView;
});
