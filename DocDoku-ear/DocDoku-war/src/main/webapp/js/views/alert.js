AlertView = BaseView.extend({
	template_el: "#alert-tpl",
	render: function () {
		$(this.el).html(this.template({
			alert: this.model
		}));
	},
});
