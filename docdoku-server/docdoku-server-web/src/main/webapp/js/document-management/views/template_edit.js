define([
	"i18n",
	"common/date",
	"views/components/modal",
	"views/template_new_attributes",
	"text!templates/template_new.html"
], function (
	i18n,
	date,
	ModalView,
	TemplateNewAttributesView,
	template
) {
	var TemplateEditView = ModalView.extend({

		template: Mustache.compile(template),

		initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
			// destroy previous template edit view if any
			if (TemplateEditView._instance) {
                TemplateEditView._oldInstance = TemplateEditView._instance;
			}
			// keep track of the created template edit view
            TemplateEditView._instance = this;
		},

		rendered: function () {
			this.attributesView = this.addSubView(
				new TemplateNewAttributesView({
                    el: "#tab-attributes-" + this.cid
				})
			);
			this.attributesView.render();
			this.attributesView.collection.reset(this.model.get("attributeTemplates"));
		},

		primaryAction: function () {
            this.model.unset("reference");
			this.model.save({
				documentType: $("#form-" + this.cid + " .type").val(),
				mask: $("#form-" + this.cid + " .mask").val(),
				idGenerated: $("#form-" + this.cid + " .id-generated").attr("checked") ? true : false,
				attributeTemplates: this.attributesView.collection.toJSON()
			}, {
				success: this.success,
				error: this.error
			});
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
	return TemplateEditView;
});
