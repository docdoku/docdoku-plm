DocumentNewTemplateListView = BaseView.extend({
	template_el: "#document-new-template-list-tpl",
	events: {
		"change": "selectionChanged",
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"selectionChanged");
		this.collection.bind("reset", this.render);
	},
	render: function () {
		$(this.el).html(this.template({
			items: this.collection.toJSON()
		}));
	},
	selectionChanged: function () {
		var templateId = $(this.el).children("select").val()
		if (templateId) {
			console.debug(templateId);
		}
	}
});
