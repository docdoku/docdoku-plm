var DocumentNewAttributeListItemView = ListItemView.extend({
	tagName: "div",
	className: "attribute-list-item",
	initialize: function () {
		ListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"change .type":		"typeChanged",
			"change .name":		"updateName",
			"change .value":	"updateValue",
			"click .remove":	"removeAction",
		});
	},
	rendered: function () {
		var type = this.model.get("type");
		this.$el.find("select.type:first").val(type);
	},
	removeAction: function () {
		this.model.destroy();
	},
	typeChanged: function (evt) {
		this.model.set({
			type: $(evt.target).val(),
		});
		this.updateValue();
		this.model.collection.trigger("reset");
	},
	updateName: function () {
		this.model.set({
			name: this.$el.find("input.name:first").val(),
		});
	},
	updateValue: function () {
		var el = this.$el.find("input.value:first");
		this.model.set({
			value: this.getValue(el)
		});
	},
	getValue: function (el) {
		return el.val();
	},
});
