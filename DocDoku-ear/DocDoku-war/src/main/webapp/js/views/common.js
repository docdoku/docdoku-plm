BaseView = Backbone.View.extend({
	template: function(data) {
		return Mustache.render(
			$(this.template_el).html(),
			data
		);
	},
});
