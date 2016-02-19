/**
 * Created by laurentlevan on 19/02/16.
 */
define([
    'backbone',
    'common-objects/views/components/modal',
    'common-objects/models/file/attached_file',
    'common-objects/views/file/file',
    'text!templates/part/part_import_form.html',
    'common-objects/views/file/file_list'

], function (Backbone, ModalView, AttachedFile, FileView, template,FileListView) {
    'use strict';
    var PartImportView = ModalView.extend({

        template: template,

        templateExtraData: {},

        tagName: 'div',
        className: 'attachedFiles idle',

        event:{
            'click form button.cancel-upload-btn': 'cancelButtonClicked',
            'change form input.upload-btn': 'fileSelectHandler',
            'dragover .droppable': 'fileDragHover',
            'dragleave .droppable': 'fileDragHover',
            'drop .droppable': 'fileDropHandler',
            'submit form':'formSubmit',
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

            /*
             if (this.options.singleFile) {
             this.listenTo(this.collection, 'add', this.addSingleFile);
             } else {
             this.listenTo(this.collection, 'add', this.addOneFile);
             }
             */

            ModalView.prototype.initialize.apply(this, arguments);
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

            _.each(e.dataTransfer.files, this.uploadNewFile.bind(this));
        },

        fileSelectHandler: function (e) {
            _.each(e.target.files, this.uploadNewFile.bind(this));
        },

        addOneFile: function (attachedFile) {
            var self = this;
            this.$toggleCheckAll.show();
            var fileView = new FileView({
                model: attachedFile,
                filesToDelete: self.filesToDelete,
                deleteBaseUrl: self.options.deleteBaseUrl,
                uploadBaseUrl: self.options.uploadBaseUrl,
                editMode: self.editMode
            });
            this.listenTo(fileView,'notification',this.printNotifications);
            this.listenTo(fileView,'clear', this.clearNotifications);
            fileView.render();
            self.filesUL.append(fileView.el);
        },

        cancelButtonClicked: function () {
            _.invoke(this.xhrs,'abort');
            //empty the array
            this.xhrs.length = 0;
            this.finished();
        },

        /*
         initAttachedFilesUploadView: function () {
         this.FileView = new FileListView({
         title: App.config.i18n.ATTACHED_FILES,
         deleteBaseUrl: this.iteration.url(),
         baseName: this.iteration.getBaseName('attachedfiles'),
         uploadBaseUrl: this.iteration.getNativeCadFileUploadBaseUrl(),
         collection: this.iteration._File,
         editMode: this.editMode,
         singleFile: true
         }).render();

         this.$('#iteration-files').html(this.FileView.el);

         },

         render:function(){
         this.initAttachedFilesUploadView();
         }*/

    });
    return PartImportView;

});
