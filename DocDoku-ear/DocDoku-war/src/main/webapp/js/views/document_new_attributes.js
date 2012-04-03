DocumentNewAttributesView = BaseView.extend({
	template_el: "#document-new-attributes-tpl",
	events: {
		"click .new-url-attribute":		"newAttribute",
	},
	collectionToJSON: function () {
		// Because collection is already JSON
		return this.collection;
	},
	newAttribute: function () {
		var template = $("#document-new-attribute-tpl").html();
		$(this.el).find(".content").first().append(
			Mustache.render(template, {})
		);
		return false;
	},
});
