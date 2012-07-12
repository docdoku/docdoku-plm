define([
	"i18n",
	"common/date"
], function (
	i18n,
	date
) {
	var DocumentIteration = Backbone.Model.extend({
		idAttribute: "iteration",
		initialize: function () {
			_.bindAll(this);
		},
		fileUploadUrl: function () {
			var doc = this.collection.document;
			return "/files/"
				+ APP_CONFIG.workspaceId
				+ "/documents/"
				+ doc.id.slice(0, doc.id.length - 2)
				+ "/"
				+ doc.id[doc.id.length - 1]
				+ "/" + this.id;
		},
	});
	return DocumentIteration;
});
