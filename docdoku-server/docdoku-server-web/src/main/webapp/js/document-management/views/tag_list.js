define([
	"collections/tag",
	"views/collapsible_list",
	"views/tag_list_item"
], function (
	TagList,
	CollapsibleListView,
	TagListItemView
) {
	var TagListView = CollapsibleListView.extend({
		itemViewFactory: function (model)  {
			return new TagListItemView({
				model: model
			});
		},
		collection: function () {
			return new TagList();
		},
		showTag: function (tag) {
			this.tag = tag;
			this.show();
		},
		shown: function () {
			if (this.tag) {
				var view = _.find(
					_.values(this.subViews),
					function (view) {
						return this.tag == view.model.id;
					},
					this
				);
				if (view) {
					view.showContent();
				}
			}
		}
	});
	return TagListView;
});
