define([
	"views/base",
	"views/document_new/document_new_attribute_list",
	"text!templates/document_new/document_new_attributes.html"
], function (
	BaseView,
	DocumentNewAttributeListView,
	template
) {
	var DocumentNewAttributesView = BaseView.extend({
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
				new DocumentNewAttributeListView({
					el: "#items-" + this.cid,
					collection: this.collection
				})
			);
		},
		addAttribute: function () {
			this.collection.add({
				name: "",
				type: "TEXT",
				value: ""
			});
		}
	});
	return DocumentNewAttributesView;
});
