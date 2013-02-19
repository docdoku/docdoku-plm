define([
	"common-objects/views/base"
], function (
	BaseView
) {
	var ListView = BaseView.extend({
		collectionReset: function () {
			this.clear();
			if (this.collection.length > 0) {
				this.render();
				this.collection.each(this.createItemView);
			}
		},
		collectionAdd: function () {
			this.collectionReset();
		},
		collectionRemove: function () {
			this.collectionReset();
		},
		createItemView: function (model) {
			var view = this.addSubView(
				this.itemViewFactory(model)
			);
			this.$el.append(view.el);
			view.render();
		}
	});
	return ListView;
});
