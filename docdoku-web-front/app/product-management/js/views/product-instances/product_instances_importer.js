/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'unorm',
    'common-objects/views/components/modal',
    'common-objects/models/file/attached_file',
    'common-objects/views/file/file',
    'common-objects/views/alert',
    'text!templates/product-instances/product_instances_import_modal.html',
    'text!templates/product-instances/product_instances_import_form.html',
    'common-objects/views/part/import_status_view'
], function (Backbone, Mustache, unorm, ModalView, AttachedFile, FileView, AlertView,modalTemplate, template, ImportStatusView) {
    'use strict';
    var ProductInstanceImportView = Backbone.View.extend({

        template: template,
        modalTemplate: modalTemplate,

        tagName: 'div',
        className: 'attachedFiles idle',

        events:{
            'click form button.cancel-upload-btn':'cancelButtonClicked',
            'change form input.upload-btn':'fileSelectHandler',
            'dragover .droppable':'fileDragHover',
            'dragleave .droppable':'fileDragHover',
            'drop .droppable':'fileDropHandler',
            'click .import-preview-button': 'showPreview',
            'click .back-button': 'backToForm',
            'click .import-button':'formSubmit',
            'hidden.bs.modal .modal.importer-view':'deleteImportStatus'
        },

        initialize: function () {

            $.event.props.push('dataTransfer');

            // Prevent browser behavior on file drop
            window.addEventListener('drop', function (e) {
                e.preventDefault();
                return false;
            }, false);

            window.addEventListener('ondragenter', function (e) {
                e.preventDefault();
                return false;
            }, false);

            this.$el.on('remove', this.removeSubviews);

            this.importForm = true;
            this.importPreview = false;
        },

        // cancel event and hover styling
        fileDragHover: function (e) {
            e.stopPropagation();
            e.preventDefault();
            if (e.type === 'dragover') {
                this.filedroparea.addClass('hover');
            }
            else {
                this.filedroparea.removeClass('hover');
            }
        },

        fileDropHandler: function (e) {
            this.fileDragHover(e);
            if (this.options.singleFile && e.dataTransfer.files.length > 1) {
                this.printNotifications('error', App.config.i18n.SINGLE_FILE_RESTRICTION);
                return;
            }

            _.each(e.dataTransfer.files, this.loadNewFile.bind(this));
        },

        fileSelectHandler: function (e) {
            _.each(e.target.files, this.loadNewFile.bind(this));
        },

        cancelButtonClicked: function () {
            //empty the file
            this.file = null;
            this.finished();
        },

        render: function () {

            this.$el.html(Mustache.render(modalTemplate, {i18n: App.config.i18n}));
            this.$el.find('#import-contain').append(Mustache.render(template, {
                importForm: this.importForm,
                importPreview: this.importPreview,
                freeze:this.freeze,
                permissive: this.permissive,
                revisionNote: this.revisionNote,
                i18n: App.config.i18n,
                options: this.options
            }));
            this.bindDomElements();
            this.fetchImports();

            return this;
        },

        rerender: function () {

            this.$el.find('#import-contain').html(Mustache.render(template, {
                importForm: this.importForm,
                importPreview: this.importPreview,
                freeze:this.freeze,
                permissive: this.permissive,
                revisionNote: this.revisionNote,
                i18n: App.config.i18n,
                options: this.options
            }));
            this.bindDomElements();

            this.$('#revision_text_part').val(this.revisionNote);
            if(this.freeze){ this.$('#freeze-checkbox').prop('checked', true); }
            if(this.permissive){ this.$('#permissive_update_product_instance-checkbox').prop('checked', true); }

            return this;
        },

        loadNewFile: function (file) {

            var fileName = unorm.nfc(file.name);

            var newFile = new AttachedFile({
                shortName: fileName
            });

            this.file = file;
            this.addOneFile(newFile);

        },

        addOneFile: function (attachedFile) {
            this.filedisplay.html('<li>' + attachedFile.getShortName() + '</li>');
            this.$('.import-preview-button').removeAttr('disabled');
        },

        bindDomElements: function () {
            this.$modal = this.$('.modal.importer-view');
            this.filedroparea = this.$('.filedroparea');
            this.filedisplay = this.$('#file-selected ul');
            this.uploadInput = this.$('input.upload-btn');
            this.progressBars = this.$('div.progress-bars');
            this.notifications = this.$('div.notifications');
        },

        showPreview: function(){
            this.freeze = this.$('#freeze-checkbox').is(':checked');
            this.permissive = this.$('#permissive_update_product_instance-checkbox').is(':checked');
            this.revisionNote = this.$('#revision_text_product').val().trim();

            this.options = this.freeze || this.permissive || this.revisionNote!== '';

            this.importForm = false;
            this.importPreview = true;
            this.rerender();

        },

        formSubmit: function () {

            this.clearNotifications();

            if (this.file) {

                var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/product-instances/import';

                var params = {
                    autoFreezeAfterUpdate: this.freeze,
                    permissiveUpdate: this.permissive,
                    revisionNote: this.revisionNote
                };

                this.deleteImportStatus();

                var importUrl = baseUrl + '?' + $.param(params);

                var xhr = new XMLHttpRequest();
                xhr.onload = this.fetchImports.bind(this);
                xhr.open('POST', importUrl);
                var formdata = new window.FormData();
                formdata.append('upload', this.file);
                xhr.send(formdata);

            } else if(!this.file){
                this.printNotifications('error', App.config.i18n.NO_FILE_TO_IMPORT);
            }

            this.backToForm();
            return false;
        },

        fetchImports:function(){
            var _this = this;
            this.removeSubviews();
            _this.$('.import-status-views').empty();

            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/import';
            $.get(url).then(function(imports){
                _.each(imports,function(pImport){
                    var view = new ImportStatusView({model:pImport}).render();
                    _this.importStatusViews.push(view);
                    _this.$('.import-status-views').append(view.$el);
                });
            });
        },

        printNotifications: function (type, message) {
            this.notifications.append(new AlertView({
                type: type,
                message: message
            }).render().$el);
        },

        clearNotifications: function () {
            this.notifications.text('');
        },

        removeSubviews: function(){
            _(this.importStatusViews).invoke('remove');
            this.importStatusViews = [];
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },

        backToForm: function () {
            this.importPreview = false;
            this.importForm = true;
            this.rerender();
            this.loadNewFile(this.file);
        },

        deleteImportStatus: function (){
            _.each(this.importStatusViews, function(importSV){
                importSV.deleteImport();
            });
        }

    });
    return ProductInstanceImportView;

});
