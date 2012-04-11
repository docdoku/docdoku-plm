var TemplateNewAttributeListItemView = ListItemView.extend({
	tagName: "div",
	className: "attribute-list-item",
	template: "template-new-attribute-list-item-tpl",
	initialize: function () {
		ListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"blur input":		"update",
			"click .remove":	"removeAction",
		});
	},
	rendered: function () {
		var attributeType = this.model.get("attributeType");
		this.$el.find("select.type:first").val(attributeType);
	},
	removeAction: function () {
		this.model.destroy();
	},
	update: function () {
		this.model.set({
			name: this.$el.find("input.name:first").val(),
		});
	},
});
