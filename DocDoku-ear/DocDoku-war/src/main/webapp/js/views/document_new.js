DocumentNewView = ModalView.extend({
	tagName: "div",
	template_el: "#document-new-tpl",
	events: {
		"submit form": "primaryAction",
	},
	initialize: function () {
		this.documentNewViewBindings();
	},
	documentNewViewBindings: function () {
		this.modalViewBindings();
		_.bindAll(this,
			"primaryAction", "success", "error");
	},
	renderAfter: function () {
		$(this.el).modal("show");
		$("#modal-form-tab").tab();
		var view = new DocumentNewTemplateListView({
			el: $("#modal-form-template-list"),
			collection: app.workspace.templates
		});
		this.subViews.push(view);
		view.collection.fetch();
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
			this.collection.create({
				reference: reference,
				title: $("#modal-form-title").val(),
				description: $("#modal-form-description").val(),
			}, {
				success: this.success,
				error: this.error
			});
		}
		return false;
	},
	success: function (model, response) {
		/*
		var iterationData = model.get("lastIteration");
		iterationData.id = iterationData.iteration;
		var iteration = new DocumentIteration(iterationData);
		iteration.save();
		*/
		$(this.el).modal("hide");
		this.collection.fetch();
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
