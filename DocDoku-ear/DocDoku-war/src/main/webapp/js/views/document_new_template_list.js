DocumentNewTemplateListView = BaseView.extend({
	collection: TemplateList,
	template: "document-new-template-select-tpl",
	initialize: function (options) {
		BaseView.prototype.initialize.apply(this, arguments);
		this.attributesView = options.attributesView;
		this.events = _.extend(this.events, {
			"change": "selectionChanged",
		});
	},
	collectionReset: function () {
		this.render();
	},
	selectionChanged: function () {
		var templateId = this.$el.find("select").first().val();
		var template = this.collection.get(templateId);
		var elId = this.parentView.$el.find("input.reference:first")
			.unmask()
			.val("");
		if (template) {
			var collection = template.get("attributeTemplates");
			if (template.get("idGenerated")) {
				this.generate_id(template);
			}
		} else {
			var collection = [];
		}
		this.attributesView.collection.reset(collection);
	},
	generate_id: function (template) {
		var elId = this.parentView.$el.find("input.reference:first");
		var mask = template.get("mask").replace(/#/g, "9");
		elId.mask(mask);
		$.get(template.url() + "/generate_id", function (data) {
			if (data) {
				elId.val(data);
			}
		}, "html");
	},
});
