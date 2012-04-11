var TagListView = CollapsibleListView.extend({
	ItemView: TagListItemView,
	collection: function () { return new TagList(); },
	showTag: function (tag) {
		this.tag = tag;
		this.show();
	},
	shown: function () {
		if (this.tag) {
			var view = _.find(_.values(this.subViews), function (view) {
				return this.tag == view.model.id
			}, this);
			if (view) view.showContent();
		}
	}
});
