FolderEditView = ModalView.extend({
	tagName: "div",
	template_el: "#folder-edit-tpl",
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
		$(this.el).find("input.name").first().val(this.model.get("name"));
		$(this.el).modal("show");
		$(this.el).find("input.name").first().focus();
		// Hide the parent's actions menu to correct a display bug
		this.parent.mouseleave();
	},
	primaryAction: function () {
		var name = $(this.el).find("input.name").first().val();
		if (name) {
			this.model.save({
				name: name
			}, {
				success: this.success,
				error: this.error
			});
		}
		return false;
	},
	success: function (model, response) {
		this.model.id = response.id;
		this.parent.render();
		if (this.parent.isOpen) this.parent.open();
		$(this.el).modal("hide");
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
