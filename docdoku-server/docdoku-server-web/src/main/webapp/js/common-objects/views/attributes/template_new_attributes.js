define([
	"common-objects/views/base",
	"common-objects/views/attributes/template_new_attribute_list",
	"text!common-objects/templates/attributes/template_new_attributes.html"
], function (
	BaseView,
	TemplateNewAttributeListView,
	template
) {
	var TemplateNewAttributesView = BaseView.extend({
		template: Mustache.compile(template),
		collection: function () {
			return new Backbone.Collection();
		},
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click .add"] = this.addAttribute;
		},
		rendered: function () {
			this.attributesView = this.addSubView(
				new TemplateNewAttributeListView({
					el: "#items-" + this.cid,
					collection: this.collection
				})
			);
		},
		addAttribute: function () {
			this.collection.add({
				name: "",
				attributeType: "TEXT"
			});
		}
	});
	return TemplateNewAttributesView;
});
