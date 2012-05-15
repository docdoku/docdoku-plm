define([
	"i18n",
	"common/date"
], function (
	i18n,
	date
) {
	var DocumentIteration = Backbone.Model.extend({
		idAttribute: "iteration",
	});
	return DocumentIteration;
});
