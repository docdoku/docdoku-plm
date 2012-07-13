define([
	"views/checkbox_list_item",
	"text!templates/workflow_list_item.html"
], function (
	CheckboxListItemView,
	template
) {
	var WorkflowListItemView = CheckboxListItemView.extend({
		template: Mustache.compile(template),
		tagName: "tr",
	});
	return WorkflowListItemView;
});
