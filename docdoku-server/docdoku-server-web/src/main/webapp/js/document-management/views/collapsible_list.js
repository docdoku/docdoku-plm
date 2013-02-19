define([
	'common-objects/views/components/list'
], function (
	ListView
) {
	var CollapsibleListView = ListView.extend({
		show: function () {
			this.$el.show();
			this.$el.addClass("in");
			var that = this;
			this.collection.fetch({
				success: function () {
					if (_.isFunction(that.shown)) {
						that.shown();
					}
				}
			});
		},
		hide: function () {
			this.$el.hide();
			this.$el.removeClass("in");
			this.clear();
		}
	});
	return CollapsibleListView;
});
