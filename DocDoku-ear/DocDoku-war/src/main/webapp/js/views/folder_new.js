FolderNewView = ModalView.extend({
	tagName: "div",
	template_el: "#folder-new-tpl",
	events: {
		"submit form": "primaryAction",
	},
	initialize: function () {
		this.modalViewBindings();
		_.bindAll(this, "success", "error");
	},
	renderAfter: function () {
		$(this.el).modal("show");
		this.nameInput = $(this.el).find("input.name").first();
		this.nameInput.focus();
	},
	primaryAction: function () {
		var name = this.nameInput.val();
		if (name) {
			this.collection.create({
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
