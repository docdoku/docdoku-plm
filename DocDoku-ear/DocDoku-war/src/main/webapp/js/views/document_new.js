DocumentNewView = BaseView.extend({
	tagName: "div",
	template_el: "#document-new-tpl",
	events: {
		"submit form": "create",
		"click .create": "create",
		"click .cancel": "cancel",
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"create", "cancel",
			"success", "error");
	},
	render: function () {
		$(this.el).html(this.template({}));
		$(this.el).modal("show");
		$(this.el).find("input.id").first().focus();
	},
	create: function () {
		var reference = $(this.el).find("input.reference").first().val();
		if (reference) {
			this.model.documents.create({
				reference: reference,
				title: $(this.el).find("input.title").first().val(),
				description: $(this.el).find("textarea.description").first().val(),
			}, {
				success: this.success,
				error: this.error
			});
		}
		return false;
	},
	success: function () {
		$(this.el).modal("hide");
		this.model.documents.fetch();
		this.remove();
	},
	error: function (model, error) {
		if (error.responseText) {
			alert(error.responseText);
		} else {
			console.error(error);
		}
	},
	cancel: function () {
		$(this.el).modal("hide");
		this.remove();
		return false;
	}
});
