var ListView = BaseView.extend({
	initialize: function () {
		this.listViewBindings();
	},
	listViewBindings: function () {
		this.baseViewBindings();
		_.bindAll(this, "createItemView");
	},
	onCollectionReset: function () {
		_.each(this.itemViews, function (view) {
			view.remove();
		});
		this.itemViews = [];
		if (this.collection.length > 0) {
			this.render();
			this.collection.each(this.createItemView);
		}
	},
	onCollectionRemove: function () {
		this.onCollectionReset();
	},
	createItemView: function (model) {
		var view = new this.ItemView({model: model});
		$(this.el).find(".items").first().append(view.el);
		this.itemViews.push(view);
		view.render();
	},
});
