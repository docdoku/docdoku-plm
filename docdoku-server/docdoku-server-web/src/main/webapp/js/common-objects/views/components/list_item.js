define([
	"common-objects/views/base"
], function (
	BaseView
) {
	var ListItemView = BaseView.extend({
		className: "list-item",
		modelDestroy: function () {
			this.destroy();
		},
		modelChange: function () {
			this.render();
		},
		modelSync: function () {
			this.render();
		}
	});
	return ListItemView;
});
