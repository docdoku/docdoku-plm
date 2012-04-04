DocumentNewView = ModalView.extend({
	template: "document-new-tpl",
	initialize: function () {
		ModalView.prototype.initialize.apply(this, arguments);
		this.events["submit #form-" + this.cid] = "primaryAction";
	},
	rendered: function () {
		this.attributesView = this.addSubView(new DocumentNewAttributeListView({
			el: "#attributes-" + this.cid,
		}));
		this.attributesView.render();
		this.templatesView = this.addSubView(new DocumentNewTemplateListView({
			el: "#templates-" + this.cid,
			attributesView: this.attributesView
		}));
		this.templatesView.collection.fetch();
	},
	primaryAction: function () {
		var reference = $("#form-" + this.cid + " .reference").val();
		if (reference) {
			this.collection.create({
				reference: reference,
				title: $("#form-" + this.cid + " .title").val(),
				description: $("#form-" + this.cid + " .description").val(),
			}, {
				success: this.success,
				error: this.error
			});
		}
		return false;
	},
	success: function (model, response) {
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
