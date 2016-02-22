/**
 * Created by laurentlevan on 19/02/16.
 */

/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'unorm',
    'common-objects/views/components/modal',
    'common-objects/models/file/attached_file',
    'common-objects/views/file/file',
    'text!templates/part/part_import_form.html'
], function (Backbone, Mustache, unorm, ModalView, AttachedFile, FileView, template) {
    'use strict';
    var PartImportView = ModalView.extend({

        template: template,

        tagName: 'div',
        className: 'attachedFiles idle',
        /*
        event:{
            'click form button.cancel-upload-btn': 'cancelButtonClicked',
            'change form input.upload-btn': 'fileSelectHandler',
            'dragover .droppable': 'fileDragHover',
            'dragleave .droppable': 'fileDragHover',
            'drop .droppable': 'fileDropHandler',
            'submit form':'formSubmit'
        },
        */
        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click form button.cancel-upload-btn']='cancelButtonClicked';
            this.events['change form input.upload-btn'] = 'fileSelectHandler';
            this.events['dragover .droppable']='fileDragHover';
            this.events['dragleave .droppable']='fileDragHover';
            this.events['drop .droppable']='fileDropHandler';
            this.events['submit form']='formSubmit';

            //$.event.props.push('dataTransfer');

            this.xhrs = [];

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

        formSubmit: function () {
            return false;
        },


        render: function () {
            /*var _this = this;*/

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));
            this.bindDomElements();

            return this;
        },

        uploadNewFile: function (file) {

            var self = this;

            var fileName = unorm.nfc(file.name);
            var progressBar = $('<div class="progress progress-striped"><div class="bar">'+fileName+'</div></div>');
            var bar = progressBar.find('.bar');
            this.progressBars.append(progressBar);

            this.gotoUploadingState();

            var newFile = new AttachedFile({
                fullName: this.options.baseName + '/' + fileName,
                shortName: fileName
            });


            /*

            var xhr = new XMLHttpRequest();

            xhr.upload.addEventListener('progress', function (evt) {
                if (evt.lengthComputable) {
                    var percentComplete = Math.round(evt.loaded * 100 / evt.total);
                    bar.width(percentComplete + '%');
                }
            }, false);

            xhr.addEventListener('load', function (e) {

                if (e.currentTarget.status !== 200 && e.currentTarget.status !== 201) {
                    self.xhrFinishedWithError(xhr, App.config.i18n.FILE + ' <' + fileName + '> : ' + e.currentTarget.statusText);
                    progressBar.remove();
                    return false;
                }

                self.xhrFinishedWithSuccess(xhr);
                progressBar.remove();
                newFile.isNew = function () {
                    return false;
                };
                var existingFile = self.filesToDelete.findWhere({fullName:newFile.getFullName()});
                if(existingFile){
                    self.filesToDelete.remove(existingFile);
                    var checkbox = self.$('[data-fullname="'+existingFile.getShortName()+'"]');
                    if(checkbox && checkbox.is(':checked')){
                        checkbox.click();
                    }
                }else{
                    self.collection.add(newFile);
                    self.newItems.add(newFile);
                }
            }, false);

            /*
            //Don't send it to the server yet
            xhr.open('POST', this.options.uploadBaseUrl);


            var fd = new window.FormData();
            fd.append('upload', file);


            xhr.send(fd);
            this.xhrs.push(xhr);
             */

            //create a view
            this.filedisplay.append(newFile.el);
        },

        bindDomElements: function () {
            this.filedroparea = this.$('.filedroparea');
            this.filedisplay = this.$('#file-selected');
            this.uploadInput = this.$('input.upload-btn');
            this.progressBars = this.$('div.progress-bars');
            this.notifications = this.$('div.notifications');
        },

        gotoUploadingState: function () {
            this.$el.removeClass('idle');
            this.$el.addClass('uploading');
        },

        xhrFinishedWithError: function(xhr, error) {
            this.printNotifications('error',error);

            this.xhrs.splice(this.xhrs.indexOf(xhr), 1);
            if (!this.xhrs.length) {
                this.gotoIdleState();
            }
        }

    });
    return PartImportView;

});
