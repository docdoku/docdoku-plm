FolderNewView = ModalView.extend({
	tagName: "div",
	template_el: "#folder-new-tpl",
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
		$(this.el).find("input.name").first().focus();
	},
	primaryAction: function () {
		var name = $(this.el).find("input.name").first().val();
		if (name) {
			this.model.folders.create({
				name: name
			}, {
				success: this.success,
				error: this.error
			});
		}
		return false;
	},
	success: function () {
		$(this.el).modal("hide");
		this.remove();
		if (this.parent.isOpen) this.parent.open();
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
