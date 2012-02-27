DocumentNewAttributesView = BaseView.extend({
	template_el: "#document-new-attributes-tpl",
	events: {
		"click .new-text-attribute":	"newTextAttribute",
		"click .new-number-attribute":	"newNumberAttribute",
		"click .new-date-attribute":	"newDateAttribute",
		"click .new-boolean-attribute":	"newBooleanAttribute",
		"click .new-url-attribute":		"newUrlAttribute",
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render");
	},
	render: function () {
		var jsonModel = this.model ? this.model.toJSON() : {};
		$(this.el).html(this.template({
			model: jsonModel
		}));
		return this;
	},
	newTextAttribute: function () {
		console.debug("newTextAttribute");
	},
	newNumberAttribute: function () {
		console.debug("newNumberAttribute");
	},
	newDateAttribute: function () {
		console.debug("newDateAttribute");
	},
	newBooleanAttribute: function () {
		console.debug("newBooleanAttribute");
	},
	newUrlAttribute: function () {
		console.debug("newUrlAttribute");
	},
});
