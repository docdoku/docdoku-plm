var DocumentNewTemplateListView = BaseView.extend({
	collection: function () { return TemplateList.getInstance(); },
	template: "document-new-template-select-tpl",
	initialize: function (options) {
		BaseView.prototype.initialize.apply(this, arguments);
		this.attributesView = options.attributesView;
		this.events = _.extend(this.events, {
			"change": "changed",
		});
	},
	collectionReset: function () {
		this.render();
	},
	collectionToJSON: function () {
		var data = BaseView.prototype.collectionToJSON.call(this);
		// Insert the empty option
		data.unshift({
			id: ""
		});
		return data;
	},
	selected: function () {
		var id = $("#select-" + this.cid).val();
		var model = this.collection.get(id);
		return model;
	},
	changed: function () {
		// Reset reference field
		this.parentView.$el.find("input.reference:first")
			.unmask()
			.val("");
		// Insert Template attributes if any
		var collection = [];
		var template = this.selected();
		if (template) {
			var attributes = template.get("attributeTemplates");
			for (var i = attributes.length - 1; i >= 0; i--){
				collection.unshift({
					type: attributes[i].attributeType,
					name: attributes[i].name,
					value: ""
				});
			};
			if (template.get("idGenerated")) {
				this.generate_id(template);
			}
		}
		this.attributesView.collection.reset(collection);
	},
	generate_id: function (template) {
		var elId = this.parentView.$el.find("input.reference:first");
		// Set field mask
		var mask = template.get("mask").replace(/#/g, "9");
		elId.mask(mask);
		// Get the next id from the webservice if any
		$.get(template.url() + "/generate_id", function (data) {
			if (data) {
				elId.val(data);
			}
		}, "html"); // TODO: fixe the webservice return type (actualy: json)
	},
});
