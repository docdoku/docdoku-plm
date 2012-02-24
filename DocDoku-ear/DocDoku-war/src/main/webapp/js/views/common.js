BaseView = Backbone.View.extend({
	template: function(data) {
		data._ = app.i18n;
		return Mustache.render(
			$(this.template_el).html(),
			data
		);
	},
	alert: function (model) {
		alertView = new AlertView({
			el: $(this.el).find(".alerts").first(),
			model: model
		});
		alertView.render();
	},
});
ModalView = BaseView.extend({
	events: {
		"click .cancel": "cancel",
	},
	cancel: function () {
		$(this.el).modal("hide");
		this.remove();
		return false;
	}
});
