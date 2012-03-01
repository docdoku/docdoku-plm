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
		this.baseViewBindings();
		console.debug(this.collection);
		console.debug(this.collectionToJSON());
	},
	collectionToJSON: function () {
		// Because collection is already JSON
		return this.collection;
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
