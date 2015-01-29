/*global define,App*/
define([
    'common-objects/utils/date',
    'common-objects/views/components/modal',
    'common-objects/views/attributes/template_new_attributes',
    'common-objects/views/file/file_list',
    'text!templates/template_new.html'
], function (date, ModalView, TemplateNewAttributesView, FileListView, template) {
	'use strict';
    var TemplateEditView = ModalView.extend({

        template: template,

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            // destroy previous template edit view if any
            if (TemplateEditView._instance) {
                TemplateEditView._oldInstance = TemplateEditView._instance;
            }
            // keep track of the created template edit view
            TemplateEditView._instance = this;

            this.events["click .modal-footer button.btn-primary"] = "interceptSubmit";
            this.events["submit form"] = "onSubmitForm";
        },

        rendered: function () {
            this.attributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: '#tab-attributes-' + this.cid,
                    attributesLocked: this.model.isAttributesLocked()
                })
            );
            this.attributesView.render();
            this.attributesView.collection.reset(this.model.get('attributeTemplates'));

            this.fileListView = new FileListView({
                deleteBaseUrl: this.model.url(),
                uploadBaseUrl: this.model.getUploadBaseUrl(),
                collection: this.model.get('attachedFiles'),
                editMode: true
            }).render();

            // Add the fileListView to the tab
            this.$('#tab-files-' + this.cid).append(this.fileListView.el);

            var $popoverLink = this.$('a#mask-help');

            $popoverLink.popover({
                title: App.config.i18n.MASK,
                placement: 'left',
                html: true,
                content: App.config.i18n.MASK_HELP,
                trigger:'manual',
                container:'.modal.new-template'
            }).click(function(e){
                $popoverLink.popover('show');
                e.stopPropagation();
                e.preventDefault();
                return false;
            });

        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            if(this.isValid){
                this.model.unset('reference');
                this.model.save({
                    documentType: this.$('#form-' + this.cid + ' .type').val(),
                    mask: this.$('#form-' + this.cid + ' .mask').val(),
                    idGenerated: this.$('#form-' + this.cid + ' .id-generated').is(':checked'),
                    attributeTemplates: this.attributesView.collection.toJSON(),
                    attributesLocked: this.attributesView.isAttributesLocked()
                }, {
                    success: this.success,
                    error: this.error
                });

                // saving new files : nothing to do : it's already saved
                // deleting unwanted files
                this.fileListView.deleteFilesToDelete();
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        cancelAction: function () {
            this.fileListView.deleteNewFiles();
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
    return TemplateEditView;
});
