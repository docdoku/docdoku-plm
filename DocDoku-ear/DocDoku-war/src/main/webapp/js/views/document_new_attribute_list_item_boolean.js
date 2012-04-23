var DocumentNewAttributeListItemBooleanView = DocumentNewAttributeListItemView.extend({
	template: "document-new-attribute-list-item-boolean-tpl",
	initialize: function () {
		DocumentNewAttributeListItemView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"change .value":	"updateValue",
		});
	},
	getValue: function (el) {
		return el.attr("checked") ? true : false;
	},
});
