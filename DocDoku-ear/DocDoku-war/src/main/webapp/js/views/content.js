var ContentView = BaseView.extend({
	el: "#content",
	initialize: function () {
		BaseView.prototype.initialize.apply(this, arguments);
		if (ContentView._instance) ContentView._instance.destroy();
		ContentView._instance = this;
	},
	destroyed: function () {
		this.$el.html("");
	},
});
