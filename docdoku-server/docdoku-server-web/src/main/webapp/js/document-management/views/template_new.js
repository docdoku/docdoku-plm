define([
	"common-objects/views/components/modal",
	"common-objects/views/attributes/template_new_attributes",
	"text!templates/template_new.html"
], function (
	ModalView,
	TemplateNewAttributesView,
	template
) {
	var TemplateNewView = ModalView.extend({
		template: Mustache.compile(template),
		initialize: function () {
			ModalView.prototype.initialize.apply(this, arguments);
		},
		rendered: function () {
			this.attributesView = this.addSubView(
				new TemplateNewAttributesView({
					el: "#tab-attributes-" + this.cid
				})
			).render();
		},
		primaryAction: function () {
			var reference = $("#form-" + this.cid + " .reference").val();
			if (reference) {
				this.collection.create({
					reference: reference,
					documentType: $("#form-" + this.cid + " .type").val(),
					mask: $("#form-" + this.cid + " .mask").val(),
					idGenerated: $("#form-" + this.cid + " .id-generated").is(':checked'),
					attributeTemplates: this.attributesView.collection.toJSON()
				}, {
                    wait: true,
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
	return TemplateNewView;
});
