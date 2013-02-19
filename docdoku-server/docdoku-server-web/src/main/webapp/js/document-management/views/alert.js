define([
	"common-objects/views/base",
	"text!templates/alert.html"
], function (
	template
) {
	var AlertView = BaseView.extend({
		template: Mustache.compile(template),
	});
	return AlertView;
});
