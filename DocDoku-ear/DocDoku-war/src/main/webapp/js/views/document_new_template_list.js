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
		if (this.attributesView) {
			this.attributesView.remove();
		}
		var templateId = $("#new-document-form-template").val()
		if (templateId) {
			this.attributesView = new DocumentNewAttributesView({
				el: $("#new-document-form .attributes"),
				model: this.collection.get(templateId)
			});
			this.attributesView.render();
		}
	}
});
