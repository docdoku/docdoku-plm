var ListView = BaseView.extend({
	initialize: function () {
		this.listViewBindings();
	},
	listViewBindings: function () {
		this.itemViews = [];
		this.baseViewBindings();
		_.bindAll(this, "createItemView");
	},
	onCollectionReset: function () {
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
		this.subViews.push(view);
		this.itemViews.push(view);
		$(this.el).find(".items").first().append(view.el);
		view.render();
	},
});
