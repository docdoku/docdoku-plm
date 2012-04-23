var TemplateListView = CheckboxListView.extend({
	collection: function () { return new TemplateList(); },
	itemViewFactory: function (model) { return new TemplateListItemView({ model: model }); },
	template: "template-list-tpl",
});
