BaseView = Backbone.View.extend({
	template: function(data) {
		data._ = app.i18n;
		return Mustache.render(
			$(this.template_el).html(),
			data
		);
	},
});
