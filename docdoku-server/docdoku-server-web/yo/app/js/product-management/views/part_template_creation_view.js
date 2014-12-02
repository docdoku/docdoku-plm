/*global define*/
define(
    [
        "common-objects/views/components/modal",
        "text!templates/part_template_creation_view.html",
        "models/part_template",
        "common-objects/views/attributes/template_new_attributes"
    ],
    function (ModalView, template, PartTemplate, TemplateNewAttributesView) {

        var PartTemplateCreationView = ModalView.extend({

            template: template,

            initialize: function () {
                ModalView.prototype.initialize.apply(this, arguments);
                this.events["click .modal-footer .btn-primary"] = "interceptSubmit";
                this.events["submit #part_template_creation_form"] = "onSubmitForm";
            },

            rendered: function () {

                this.bindDomElements();

                this.attributesView = this.addSubView(
                    new TemplateNewAttributesView({
                        el: "#tab-attributes"
                    })
                ).render();

                this.$("a#mask-help").popover({
                    title: App.config.i18n.MASK,
                    placement: "left",
                    html: true,
                    content: App.config.i18n.MASK_HELP
                });

                this.$partTemplateReference.customValidity(App.config.i18n.REQUIRED_FIELD);

            },

            bindDomElements: function () {
                this.$partTemplateReference = this.$("#part-template-reference");
                this.$partTemplateType = this.$("#part-template-type");
                this.$partTemplateMask = this.$("#part-template-mask");
                this.$partTemplateIdGenerated = this.$("#part-template-id-generated");
            },

            interceptSubmit:function(){
                this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
            },

            onSubmitForm: function (e) {

                this.model = new PartTemplate({
                    reference: this.$partTemplateReference.val(),
                    partType: this.$partTemplateType.val(),
                    mask: this.$partTemplateMask.val(),
                    idGenerated: this.$partTemplateIdGenerated.is(":checked"),
                    attributeTemplates: this.attributesView.collection.toJSON(),
                    attributesLocked: this.attributesView.isAttributesLocked()
                });

                this.model.save({}, {
                    wait: true,
                    success: this.onPartTemplateCreated,
                    error: this.onError
                });

                e.preventDefault();
                e.stopPropagation();
                return false;
            },

            onPartTemplateCreated: function () {
                this.trigger('part-template:created', this.model);
                this.hide();
            },

            onError: function (model, error) {
                alert(App.config.i18n.CREATION_ERROR + " : " + error.responseText);
            }

        });

        return PartTemplateCreationView;

    });
