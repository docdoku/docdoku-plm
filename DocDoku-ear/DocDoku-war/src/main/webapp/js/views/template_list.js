var TemplateListView = CheckboxListView.extend({
	collection: function () { return new TemplateList(); },
	ItemView: TemplateListItemView,
	template: "template-list-tpl",
});
