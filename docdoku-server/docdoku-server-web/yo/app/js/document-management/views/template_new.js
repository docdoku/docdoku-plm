/*global define,App*/
define([
    'common-objects/views/components/modal',
    'common-objects/views/workflow/workflow_list',
    'common-objects/views/attributes/template_new_attributes',
    'text!templates/template_new.html'
], function (ModalView, DocumentWorkflowListView, TemplateNewAttributesView, template) {
    'use strict';
    var TemplateNewView = ModalView.extend({

        template: template,

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click .modal-footer button.btn-primary'] = 'interceptSubmit';
            this.events['submit form'] = 'onSubmitForm';
        },

        rendered: function () {

            this.workflowsView = this.addSubView(
                new DocumentWorkflowListView({
                    el: '#workflows-' + this.cid
                })
            );

            this.attributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: '#tab-attributes-' + this.cid,
                    editMode: true
                })
            ).render();

            var $popoverLink = this.$('#mask-help');

            $popoverLink.popover({
                title: App.config.i18n.MASK,
                placement: 'left',
                html: true,
                trigger: 'manual',
                content: App.config.i18n.MASK_HELP.nl2br(),
                container:'.modal.new-template'
            }).click(function(e){
                $popoverLink.popover('show');
                e.stopPropagation();
                e.preventDefault();
                return false;
            });

            this.$('input.reference').customValidity(App.config.i18n.REQUIRED_FIELD);

        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {
            if (this.isValid) {
                var workflow = this.workflowsView.selected();

                this.collection.create({
                    reference:  this.$('#form-' + this.cid + ' .reference').val(),
                    documentType: this.$('#form-' + this.cid + ' .type').val(),
                    mask: this.$('#form-' + this.cid + ' .mask').val(),
                    idGenerated: this.$('#form-' + this.cid + ' .id-generated').is(':checked'),
                    workflowModelId: workflow ? workflow.get('id') : null,
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
        success: function () {
            this.hide();
        },
        error: function (model, error) {
            this.collection.remove(model);
            if (error.responseText) {
                this.alert({
                    type: 'error',
                    message: error.responseText
                });
            } else {
                console.error(error);
            }
        }
    });
    return TemplateNewView;
});
