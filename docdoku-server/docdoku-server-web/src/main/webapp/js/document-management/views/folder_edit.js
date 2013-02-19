define([
	"common-objects/views/components/modal",
	"text!templates/folder_edit.html"
], function (
	ModalView,
	template
) {
	var FolderEditView = ModalView.extend({
		template: Mustache.compile(template),
		tagName: "div",
		initialize: function () {
			ModalView.prototype.initialize.apply(this, arguments);
			this.events["submit form"] = "primaryAction";
		},
		rendered: function () {
			this.nameInput = this.$el.find("input.name").first();
			this.nameInput.val(this.model.get("name"));
		},
		primaryAction: function () {
			var name = this.nameInput.val();
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
			this.hide();
		},
		error: function (model, error) {
			if (error.responseText) {
				this.alert({
					type: "error",
					message: error.responseText
				});
			} else {
				console.error(error);
			}
		}
	});
	return FolderEditView;
});
