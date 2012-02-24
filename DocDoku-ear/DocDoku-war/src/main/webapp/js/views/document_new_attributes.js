DocumentNewAttributesView = BaseView.extend({
	template_el: "#document-new-attributes-tpl",
	initialize: function () {
		_.bindAll(this,
			"template", "render");
	},
	render: function () {
		console.debug(this.model.toJSON());
		$(this.el).html(this.template({
			model: this.model.toJSON()
		}));
		return this;
	},
});
