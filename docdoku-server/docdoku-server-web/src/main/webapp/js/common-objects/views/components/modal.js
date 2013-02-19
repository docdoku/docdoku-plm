define([
	"common-objects/views/base"
], function (
	BaseView
) {
    /**
     * A Modal window may have a primaryAction function bind on a ".btn-primary" button
     */
	var ModalView = BaseView.extend({
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			if (this.cancelAction) {
				this.events["click .modal-footer .cancel"] = "cancelAction";
			}
			if (this.primaryAction) {
				this.events["click .modal-footer .btn-primary"] = "primaryAction";
			}
			this.$el.one("shown", this.shown);
			this.$el.one("hidden", this.hidden);
			this.show();
		},
		show: function () {
			this.$el.modal("show");
		},
		shown: function () {
			this.render();
		},
		hide: function () {
			this.$el.modal("hide");
			// Sometimes the destroy is too fast.
			// Bootstrap should have thrown hidden only when all is finished
			// see: bootstrap hideModal
			$(".modal-backdrop").remove(); // TODO: Fin a way to remove the hack.
		},
		hidden: function () {
			this.destroy();
		},
		cancelAction: function () {
			this.hide();
			return false;
		}
	});
	return ModalView;
});
