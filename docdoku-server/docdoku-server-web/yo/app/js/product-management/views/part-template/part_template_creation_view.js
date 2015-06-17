/*global define,App*/
define([
    'common-objects/views/components/modal',
    'text!templates/part-template/part_template_creation_view.html',
    'models/part_template',
    'common-objects/views/alert',
    'common-objects/views/attributes/template_new_attributes',
    'common-objects/views/workflow/workflow_list'
], function (ModalView, template, PartTemplate, AlertView, TemplateNewAttributesView, WorkflowListView) {
    'use strict';
    var PartTemplateCreationView = ModalView.extend({
        template: template,

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click .modal-footer .btn-primary'] = 'interceptSubmit';
            this.events['submit #part_template_creation_form'] = 'onSubmitForm';
        },

        rendered: function () {

            this.bindDomElements();

            this.workflowsView = new WorkflowListView({
                el: this.$('#workflows-list')
            });

            this.productInstanceAttributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: '#attribute-product-instance-list',
                    editMode: true,
                    unfreezable: true
                })
            ).render();

            this.attributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: '#attributes-list',
                    editMode: true,
                    attributesLocked: this.attributesLocked
                })
            ).render();


            var $popoverLink = this.$('#mask-help');

            $popoverLink.popover({
                title: App.config.i18n.MASK,
                placement: 'left',
                html: true,
                trigger: 'manual',
                content: App.config.i18n.MASK_HELP.nl2br(),
                container:'#part_template_creation_modal'
            }).click(function(e){
                $popoverLink.popover('show');
                e.stopPropagation();
                e.preventDefault();
                return false;
            });

            this.$partTemplateReference.customValidity(App.config.i18n.REQUIRED_FIELD);

        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$partTemplateReference = this.$('#part-template-reference');
            this.$partTemplateType = this.$('#part-template-type');
            this.$partTemplateMask = this.$('#part-template-mask');
            this.$partTemplateIdGenerated = this.$('#part-template-id-generated');
        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            if(this.isValid){
                var workflow = this.workflowsView.selected();

                this.model = new PartTemplate({
                    reference: this.$partTemplateReference.val(),
                    partType: this.$partTemplateType.val(),
                    mask: this.$partTemplateMask.val(),
                    idGenerated: this.$partTemplateIdGenerated.is(':checked'),
                    attributeTemplates: this.attributesView.collection.toJSON(),
                    attributeInstanceTemplates: this.productInstanceAttributesView.collection.toJSON(),
                    attributesLocked: this.attributesView.isAttributesLocked(),
                    workflowModelId: workflow ? workflow.get('id') : null
                });

                this.model.save({}, {
                    wait: true,
                    success: this.onPartTemplateCreated,
                    error: this.onError
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onPartTemplateCreated: function () {
            this.trigger('part-template:created', this.model);
            this.hide();
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        }

    });

    return PartTemplateCreationView;

});
