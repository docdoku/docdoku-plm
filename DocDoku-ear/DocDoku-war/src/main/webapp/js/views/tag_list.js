TagListView = ListView.extend({
	ItemView: TagListItemView,
	tagName: "ul",
	template_el: "#tag-list-tpl",
	initialize: function () {
		this.listViewBindings();
	},
});
