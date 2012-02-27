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
		this.attributesView = new DocumentNewAttributesView({
			el: $("#modal-form-tab-attributes"),
			model: null
		});
		this.attributesView.render();
	},
	selectionChanged: function () {
		var templateId = $("#modal-form-template").val()
		this.attributesView = new DocumentNewAttributesView({
			el: $("#modal-form-tab-attributes"),
			model: this.collection.get(templateId)
		});
		this.attributesView.render();
	}
});
