/*global define,App*/
define([
    'common-objects/views/components/modal',
    'text!templates/part-template/part_template_creation_view.html',
    'models/part_template',
    'common-objects/views/file/file_list',
    'common-objects/views/attributes/template_new_attributes',
    'common-objects/views/workflow/workflow_list',
    'common-objects/views/alert',
    'common-objects/utils/date'
], function (ModalView, template, PartTemplate, FileListView, TemplateNewAttributesView, WorkflowListView, AlertView, date) {
	'use strict';
    var PartTemplateEditView = ModalView.extend({

        template: template,

        initialize: function () {
            this.templateExtraData = {modificationDate : this.model.getFormattedModificationDate()};
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click .modal-footer .btn-primary'] = 'interceptSubmit';
            this.events['submit #part_template_creation_form'] = 'onSubmitForm';
        },

        rendered: function () {

            this.bindDomElements();

            this.workflowsView = new WorkflowListView({
                el: this.$('#workflows-list')
            });

            var _this = this;
            this.workflowsView.collection.on('reset', function() {
                _this.workflowsView.setValue(_this.model.get('workflowModelId'));
            });

            this.attributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: '#attributes-list',
                    attributesLocked: this.model.isAttributesLocked(),
                    editMode:true
                })
            ).render();

            this.productInstanceAttributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: '#attribute-product-instance-list',
                    editMode: true,
                    unfreezable: true
                })
            ).render();


            this.fileListView = new FileListView({
                baseName: this.model.getBaseName(),
                deleteBaseUrl: this.model.url(),
                uploadBaseUrl: this.model.getUploadBaseUrl(),
                collection: this.model._attachedFile,
                editMode: true,
                singleFile: true
            }).render();

            this.$('#tab-files').append(this.fileListView.el);

            this.attributesView.collection.reset(this.model.get('attributeTemplates'));
            this.productInstanceAttributesView.collection.reset(this.model.get('attributeInstanceTemplates'));

            var $popoverLink = this.$('a#mask-help');

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
            date.dateHelper(this.$('.date-popover'));
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$partTemplateReference = this.$('#part-template-reference');
            this.$partTemplateType = this.$('#part-template-type');
            this.$partTemplateMask = this.$('#part-template-mask');
            this.$partTemplateIdGenerated = this.$('#part-template-id-generated');
            this.tabs =this.$('.nav-tabs li');
        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            if(this.isValid){
                var workflow = this.workflowsView.selected();

                // cannot pass a collection of cad file to server.
                var attachedFile = this.fileListView.collection.first();
                this.fileListView.collection.reset(this.fileListView.collection.last());
                if (attachedFile) {
                    this.model.set('attachedFile', attachedFile.get('fullName'));
                } else {
                    this.model.set('attachedFile', '');
                }

                this.model.save({
                    id: this.$partTemplateReference.val(),
                    partType: this.$partTemplateType.val(),
                    mask: this.$partTemplateMask.val(),
                    idGenerated: this.$partTemplateIdGenerated.is(':checked'),
                    attributeTemplates: this.attributesView.collection.toJSON(),
                    attributeInstanceTemplates: this.productInstanceAttributesView.collection.toJSON(),
                    attributesLocked: this.attributesView.isAttributesLocked(),
                    workflowModelId: workflow ? workflow.get('id') : null
                }, {
                    wait: true,
                    success: this.onPartTemplateSaved,
                    error: this.onError
                });

                this.fileListView.deleteFilesToDelete();
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        activateTab: function (index) {

            this.tabs.eq(index).children().tab('show');
        },
        activateFileTab: function(){
            this.activateTab(3);
        },
        cancelAction: function () {
            this.fileListView.deleteNewFiles();
        },

        onPartTemplateSaved: function () {
            this.model.fetch();
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

    return PartTemplateEditView;

});
