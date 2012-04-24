var TemplateNewAttributeListItemView = ListItemView.extend({
	tagName: "div",
	className: "attribute-list-item",
	template: "template-new-attribute-list-item-tpl",
	initialize: function () {
		ListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"change .type":		"typeChanged",
			"change .name":		"updateName",
			"click .remove":	"removeAction",
		});
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
			attributeType: $(evt.target).val(),
		});
	},
	updateName: function () {
		this.model.set({
			name: this.$el.find("input.name:first").val(),
		});
	},
});
