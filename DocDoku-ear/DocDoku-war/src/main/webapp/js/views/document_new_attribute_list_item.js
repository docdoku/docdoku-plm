DocumentNewAttributeListItemView = ListItemView.extend({
	tagName: "div",
	className: "document-new-attribute-list-item",
	template: "document-new-attribute-list-item-text-tpl",
	template: function () {
		var attributeTypeTemplateMapping = {
			"BOOLEAN":	"document-new-attribute-list-item-boolean-tpl",
			"DATE":		"document-new-attribute-list-item-date-tpl",
			"NUMBER":	"document-new-attribute-list-item-number-tpl",
			"TEXT":		"document-new-attribute-list-item-text-tpl",
			"URL":		"document-new-attribute-list-item-url-tpl",
		};
		var type = this.model.get("attributeType") ? this.model.get("attributeType") : "TEXT";
		return attributeTypeTemplateMapping[type];
	},
	initialize: function () {
		ListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"change .type": "setType",
			"click .remove": "removeAction",
		});
	},
	rendered: function () {
		var attributeType = this.model.get("attributeType");
		var elType = this.$el.find("select.type").first();
		elType.val(attributeType);
	},
	removeAction: function (evt) {
		this.model.destroy();
	},
	setType: function (evt) {
		var elType = $(evt.target);
		var elName = this.$el.find("input.name").first();
		var elValue = this.$el.find("input.value").first();
		var attributeType = elType.val();
		this.model.set({
			attributeType: elType.val(),
			name: elName.val(),
			value: elValue.val()
		});
		this.render();
	},
});
