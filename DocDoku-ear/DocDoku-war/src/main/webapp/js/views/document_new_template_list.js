DocumentNewTemplateListView = BaseView.extend({
	template_el: "#document-new-template-list-tpl",
	events: {
		"change": "selectionChanged",
	},
	initialize: function () {
		console.debug("initialize");
		this.baseViewBindings();
		_.bindAll(this, "selectionChanged");
	},
	renderAfter: function () {
		var view = new DocumentNewAttributesView({
			el: $("#modal-form-tab-attributes"),
		});
		this.subViews.push(view);
		view.render();
	},
	onCollectionReset: function () {
		this.render();
	},
	selectionChanged: function () {
		var templateId = $("#modal-form-template").val()
		var view = new DocumentNewAttributesView({
			el: $("#modal-form-tab-attributes"),
			model: this.collection.get(templateId)
		});
		this.subViews.push(view);
		view.render();
	}
});
