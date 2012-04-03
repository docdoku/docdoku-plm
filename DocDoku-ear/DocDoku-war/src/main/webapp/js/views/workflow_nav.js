var WorkflowNavView = BaseView.extend({
	template: "#workflow-nav-tpl",
	el: "#workflow-nav",
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
		this.addSubView(new WorkflowContentListView()).render();
	},
});
WorkflowNavView = singletonDecorator(WorkflowNavView);
