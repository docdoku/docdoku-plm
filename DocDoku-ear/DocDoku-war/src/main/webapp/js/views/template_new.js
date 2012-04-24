TemplateNewView = ModalView.extend({
	template: "template-new-tpl",
	initialize: function () {
		ModalView.prototype.initialize.apply(this, arguments);
		this.events["submit #form-" + this.cid] = "primaryAction";
	},
	rendered: function () {
		this.attributesView = this.addSubView(new TemplateNewAttributesView({
			el: "#tab-attributes-" + this.cid,
		}));
		this.attributesView.render();
	},
	primaryAction: function () {
		var reference = $("#form-" + this.cid + " .reference").val();
		if (reference) {
			this.collection.create({
				reference: reference,
				documentType: $("#form-" + this.cid + " .title").val(),
				mask: $("#form-" + this.cid + " .mask").val(),
				idGenerated: $("#form-" + this.cid + " .id-generated")
					.attr("checked") ? true : false,
				attributeTemplates: this.attributesView.collection.toJSON()
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
		this.collection.remove(model);
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
