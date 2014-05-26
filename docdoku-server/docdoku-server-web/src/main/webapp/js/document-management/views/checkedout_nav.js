/*global APP_VIEW*/
define([
	"common-objects/common/singleton_decorator",
	"common-objects/views/base",
	"views/checked_out_document_list",
	"text!templates/checkedout_nav.html"
], function (
	singletonDecorator,
	BaseView,
	CheckedoutContentListView,
	template
) {
	var CheckedOutNavView = BaseView.extend({
		template: Mustache.compile(template),
		el: "#checked-out-nav",
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.render();
		},
		setActive: function () {
            if(APP_VIEW.$documentManagementMenu){
                APP_VIEW.$documentManagementMenu.find(".active").removeClass("active");
            }
			this.$el.find(".nav-list-entry").first().addClass("active");
		},
		showContent: function () {
			this.setActive();
			this.addSubView(
				new CheckedoutContentListView()
			).render();
		}
	});
    CheckedOutNavView = singletonDecorator(CheckedOutNavView);
	return CheckedOutNavView;
});
