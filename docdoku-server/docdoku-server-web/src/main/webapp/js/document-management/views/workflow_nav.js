define([
	"common/singleton_decorator",
	"views/base",
	"views/workflow_content_list",
	"text!templates/workflow_nav.html"
], function (
	singletonDecorator,
	BaseView,
	WorkflowContentListView,
	template
) {
	var WorkflowNavView = BaseView.extend({
		template: Mustache.compile(template),
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
			this.addSubView(
				new WorkflowContentListView()
			).render();
		}
	});
	WorkflowNavView = singletonDecorator(WorkflowNavView);
	return WorkflowNavView;
});
