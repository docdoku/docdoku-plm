define([
	"common-objects/views/components/list_item",
	"text!common-objects/templates/attributes/template_new_attribute_list_item.html"
], function (
	ListItemView,
	template
) {
	var TemplateNewAttributeListItemView = ListItemView.extend({
		template: Mustache.compile(template),
		tagName: "div",
		initialize: function () {
			ListItemView.prototype.initialize.apply(this, arguments);
			this.events["change .type"] = "typeChanged";
			this.events["change .name"] = "updateName";
			this.events["click .remove"] = "removeAction";
		},
		rendered: function () {
			var type = this.model.get("attributeType");
			this.$el.find("select.type:first").val(type);
		},
		removeAction: function () {
			this.model.destroy();
		},
		typeChanged: function (evt) {
			this.model.set({
				attributeType: $(evt.target).val()
			});
		},
		updateName: function () {
			this.model.set({
				name: this.$el.find("input.name:first").val()
			});
		}
	});
	return TemplateNewAttributeListItemView;
});
