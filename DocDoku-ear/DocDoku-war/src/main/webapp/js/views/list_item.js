define([
	"views/base"
], function (
	BaseView
) {
	var ListItemView = BaseView.extend({
		modelDestroy: function () {
			this.destroy();
		},
		modelChange: function () {
			this.render();
		},
		modelSync: function () {
			this.render();
		},
	});
	return ListItemView;
});
