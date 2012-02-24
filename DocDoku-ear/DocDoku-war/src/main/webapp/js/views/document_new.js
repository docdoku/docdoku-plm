DocumentNewView = ModalView.extend({
	tagName: "div",
	template_el: "#document-new-tpl",
	events: {
		"submit form": "primaryAction",
		"click .btn-primary": "primaryAction",
		"click .cancel": "cancel",
	},
	initialize: function () {
		_.bindAll(this,
			"template", "render",
			"primaryAction", "success", "error");
	},
	render: function () {
		$(this.el).html(this.template({}));
		$(this.el).modal("show");
		$(this.el).find("input.id").first().focus();
		templateListView = new DocumentNewTemplateListView({
			el: $("#modal-form-template-list"),
			collection: app.workspace.templates
		});
		templateListView.collection.fetch();
		$("#modal-form-tab").tab();
	},
	getAttributes: function () {
		var attributes = {}
		$(this.el).find("input.attribute").each(function () {
			console.debug($(this).attr("name"), $(this).val());
		});
		return attributes;
	},
	primaryAction: function () {
		var reference = $("#modal-form-reference").val();
		if (reference) {
			this.model.documents.create({
				reference: reference,
				title: $("#modal-form-title").val(),
				description: $("#modal-form-description").val()
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
			this.alert({
				type: "error",
				title: "Erreur",
				message: error.responseText
			});
		} else {
			console.error(error);
		}
	}
});
