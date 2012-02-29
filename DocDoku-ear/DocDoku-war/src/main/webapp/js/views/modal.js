ModalView = BaseView.extend({
	events: {},
	modalViewBindings: function () {
		this.baseViewBindings();
		if (this.cancelAction) {
			_.bindAll(this, "cancelAction");
			this.events["click .modal-footer .cancel"] = "cancelAction";
		}
		if (this.primaryAction) {
			_.bindAll(this, "primaryAction");
			this.events["click .modal-footer .btn-primary"] = "primaryAction";
		}
		if (this.onHidden) {
			_.bindAll(this, "onHidden");
		};
	},
	cancelAction: function () {
		$(this.el).modal("hide");
		this.remove();
		return false;
	}
});
