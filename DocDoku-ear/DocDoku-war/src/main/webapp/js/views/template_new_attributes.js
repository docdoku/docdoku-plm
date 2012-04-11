TemplateNewAttributesView = BaseView.extend({
	collection: function () { return new Backbone.Collection(); },
	template: "template-new-attributes-tpl",
	initialize: function () {
		ListView.prototype.initialize.apply(this, arguments);
		this.events["click .add"] = this.addAttribute;
	},
	rendered: function () {
		this.attributesView = this.addSubView(new TemplateNewAttributeListView({
			el: "#items-" + this.cid,
			collection: this.collection
		}));
	},
	addAttribute: function () {
		this.collection.add({
			name: "",
			attributeType: "TEXT",
		});
	},
});
