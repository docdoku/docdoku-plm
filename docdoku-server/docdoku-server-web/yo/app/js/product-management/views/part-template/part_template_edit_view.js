/*global define,App*/
define([
    'common-objects/views/components/modal',
    'text!templates/part-template/part_template_creation_view.html',
    'models/part_template',
    'common-objects/views/file/file_list',
    'common-objects/views/attributes/template_new_attributes',
    'common-objects/views/alert'
], function (ModalView, template, PartTemplate, FileListView, TemplateNewAttributesView, AlertView) {
	'use strict';
    var PartTemplateEditView = ModalView.extend({

        template: template,

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events["click .modal-footer .btn-primary"] = "interceptSubmit";
            this.events['submit #part_template_creation_form'] = 'onSubmitForm';
        },

        rendered: function () {

            this.bindDomElements();

            this.attributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: '#tab-attributes',
                    attributesLocked: this.model.isAttributesLocked()
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

            this.$('a#mask-help').popover({
                title: App.config.i18n.MASK,
                placement: 'left',
                html: true,
                content: App.config.i18n.MASK_HELP.nl2br()
            });

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
                // cannot pass a collection of cad file to server.
                var attachedFile = this.fileListView.collection.first();
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
                    attributesLocked: this.attributesView.isAttributesLocked()
                }, {
                    wait: true,
                    success: this.onPartTemplateCreated,
                    error: this.onError
                });

                this.fileListView.deleteFilesToDelete();
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        cancelAction: function () {
            this.fileListView.deleteNewFiles();
        },

        onPartTemplateCreated: function () {
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
