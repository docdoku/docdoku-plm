DocumentNewAttributeListItemView = ListItemView.extend({
	tagName: "div",
	className: "document-new-attribute-list-item",
	template: "#document-new-attribute-list-item-tpl",
	attributeTypeMapping: {
		"BOOLEAN":	"boolean",
		"DATE":		"date",
		"NUMBER":	"number",
		"TEXT":		"text",
		"URL":		"url",
	},
	initialize: function () {
		ListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"change .type": "setType",
			"click .remove": "removeAction",
		});
	},
	modelToJSON: function () {
		var data = this.model.toJSON();
		if (this.attributeTypeMapping[data.attributeType]) {
			data.type = this.attributeTypeMapping[data.attributeType];
		} else {
			data.type = "text";
		}
		return data;
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
