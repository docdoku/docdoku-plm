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
		var template = this.collection.get(templateId);
		var collection = template ? template.get("attributeTemplates") : null;
		console.debug(template.toJSON());
		var view = new DocumentNewAttributesView({
			el: $("#modal-form-tab-attributes"),
			collection: collection 
		});
		this.subViews.push(view);
		view.render();
	}
});
