/*global define*/
define([
    "common-objects/views/components/modal",
    "common-objects/views/attributes/template_new_attributes",
    "text!templates/template_new.html"
], function (ModalView, TemplateNewAttributesView, template) {
    var TemplateNewView = ModalView.extend({

        template: template,
        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events["click .modal-footer button.btn-primary"] = "interceptSubmit";
            this.events["submit form"] = "onSubmitForm";
        },
        rendered: function () {
            this.attributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: "#tab-attributes-" + this.cid
                })
            ).render();

            this.$("a#mask-help").popover({
                title: App.config.i18n.MASK,
                placement: "left",
                html: true,
                content: App.config.i18n.MASK_HELP
            });

            this.$('input.reference').customValidity(App.config.i18n.REQUIRED_FIELD);

        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {
            if (this.isValid) {
                this.collection.create({
                    reference:  this.$("#form-" + this.cid + " .reference").val(),
                    documentType: this.$("#form-" + this.cid + " .type").val(),
                    mask: this.$("#form-" + this.cid + " .mask").val(),
                    idGenerated: this.$("#form-" + this.cid + " .id-generated").is(':checked'),
                    attributeTemplates: this.attributesView.collection.toJSON(),
                    attributesLocked: this.attributesView.isAttributesLocked()
                }, {
                    wait: true,
                    success: this.success,
                    error: this.error
                });
            }
            e.preventDefault();
            e.stopPropagation();

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
