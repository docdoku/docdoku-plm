define([
	"common-objects/views/base",
	"text!templates/alert.html"
], function (
    BaseView,
	template
) {
	var AlertView = BaseView.extend({
		template: Mustache.compile(template)
	});
	return AlertView;
});
