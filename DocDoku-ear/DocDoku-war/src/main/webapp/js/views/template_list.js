var TemplateListView = CheckboxListView.extend({
	itemViewFactory: function (model) { return new TemplateListItemView({ model: model }); },
	template: "template-list-tpl",
});
