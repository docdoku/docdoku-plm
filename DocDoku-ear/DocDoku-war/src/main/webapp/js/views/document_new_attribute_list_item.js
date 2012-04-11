var DocumentNewAttributeListItemView = ListItemView.extend({
	tagName: "div",
	className: "attribute-list-item",
	template: function () {
		var attributeTypeTemplateMapping = {
			"BOOLEAN":	"document-new-attribute-list-item-boolean-tpl",
			"DATE":		"document-new-attribute-list-item-date-tpl",
			"NUMBER":	"document-new-attribute-list-item-number-tpl",
			"TEXT":		"document-new-attribute-list-item-text-tpl",
			"URL":		"document-new-attribute-list-item-url-tpl",
		};
		var type = this.model.get("type") ? this.model.get("type") : "TEXT";
		return attributeTypeTemplateMapping[type];
	},
	initialize: function () {
		ListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"change select":	"typeChanged",
			"blur input":		"update",
			"click .remove":	"removeAction",
		});
	},
	rendered: function () {
		var attributeType = this.model.get("type");
		this.$el.find("select.type:first").val(attributeType);
	},
	removeAction: function () {
		this.model.destroy();
	},
	typeChanged: function (evt) {
		this.model.set({
			type: $(evt.target).val(),
			name: this.$el.find("input.name:first").val(),
			value: this.$el.find("input.value:first").val()
		});
		this.render();
	},
	update: function () {
		this.model.set({
			name: this.$el.find("input.name:first").val(),
			value: this.$el.find("input.value:first").val()
		});
	},
});
