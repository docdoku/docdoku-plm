var CheckedoutNavView = BaseView.extend({
	template: "checkedout-nav-tpl",
	el: "#checkedout-nav",
	initialize: function () {
		BaseView.prototype.initialize.apply(this, arguments);
		this.render();
	},
	setActive: function () {
		$("#nav .active").removeClass("active");
		this.$el.find(".nav-list-entry").first().addClass("active");
	},
	showContent: function () {
		this.setActive();
		this.addSubView(new CheckedoutContentListView()).render();
	},
});
CheckedoutNavView = singletonDecorator(CheckedoutNavView);
