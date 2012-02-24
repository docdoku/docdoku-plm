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
		templateListView = new DocumentNewTemplateListView({
			el: $("#new-document-form-template-list"),
			collection: app.workspace.templates
		});
		templateListView.collection.fetch();
	},
	getAttributes: function () {
		var attributes = {}
		$(this.el).find("input.attribute").each(function () {
			console.debug($(this).attr("name"), $(this).val());
		});
		return attributes;
	},
	create: function () {
		var reference = $("#new-document-form-reference").val();
		if (reference) {
			this.model.documents.create({
				reference: reference,
				title: $("#new-document-form-title").val(),
				description: $("#new-document-form-description").val()
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
