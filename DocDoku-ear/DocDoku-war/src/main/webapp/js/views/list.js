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
		var view = this.addSubView(this.itemViewFactory(model));
		this.$el.append(view.el);
		view.render();
	},
});

var CollapsibleListView = ListView.extend({
	show: function () {
		this.$el.show();
		this.$el.addClass("in");
		var that = this;
		this.collection.fetch({
			success: function () {
				if (_.isFunction(that.shown)) that.shown();
			}
		});
	},
	hide: function () {
		this.$el.hide();
		this.$el.removeClass("in");
		this.clear();
	},
});

var CheckboxListView = ListView.extend({
	initialize: function () {
		ListView.prototype.initialize.apply(this, arguments);
		this.checkToggle = "#check-toggle-" + this.cid;
		this.events["click " + this.checkToggle] = "toggle";
	},
	toggle: function () {
		if ($(this.checkToggle).is(":checked")) {
			_.each(_.values(this.subViews), function (view) {
				view.check();
			});
		} else {
			_.each(_.values(this.subViews), function (view) {
				view.uncheck();
			});
		}
	},
	viewAdded: function (view) {
		var that = this;
		view.on("checked unchecked", function () {
			that.trigger("selectionChange");
		});
	},
	checkedViews: function () {
		return _.filter(_.values(this.subViews),
			function (view) {
				return view.isChecked;
			});
	},
	eachChecked: function (callback) {
		_.each(this.checkedViews(), callback);
	}
});
