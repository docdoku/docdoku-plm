var TemplateNavView = BaseView.extend({
	template: "#template-nav-tpl",
	el: "#template-nav",
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
		this.addSubView(new TemplateContentListView()).render();
	},
});
TemplateNavView = singletonDecorator(TemplateNavView);
