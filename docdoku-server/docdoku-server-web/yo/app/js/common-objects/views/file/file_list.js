/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/file/file_list.html',
    'common-objects/models/file/attached_file',
    'common-objects/views/file/file'
], function (Backbone, Mustache, template, AttachedFile, FileView) {
    'use strict';
	var FileListView = Backbone.View.extend({

        tagName: 'div',
        className: 'attachedFiles idle',

        editMode: true,

        events: {
            'click form button.cancel-upload-btn': 'cancelButtonClicked',
            'change form input#upload-btn': 'fileSelectHandler',
            'dragover .droppable': 'fileDragHover',
            'dragleave .droppable': 'fileDragHover',
            'drop .droppable': 'fileDropHandler'
        },

        initialize: function () {
            this.editMode = this.options.editMode;

            // jQuery creates it's own event object, and it doesn't have a
            // dataTransfer property yet. This adds dataTransfer to the event object.
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

            this.filesToDelete = new Backbone.Collection();
            this.newItems = new Backbone.Collection();

            if (this.options.singleFile) {
                this.listenTo(this.collection, 'add', this.addSingleFile);
            } else {
                this.listenTo(this.collection, 'add', this.addOneFile);
            }
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
            this.uploadNewFile(e.dataTransfer.files[0]);
        },

        fileSelectHandler: function (e) {
            this.uploadNewFile(e.target.files[0]);
        },

        addAllFiles: function () {
            this.collection.each(this.addOneFile, this);
        },

        addOneFile: function (attachedFile) {
            var self = this;


            var fileView = new FileView({
                model: attachedFile,
                filesToDelete: self.filesToDelete,
                deleteBaseUrl: self.options.deleteBaseUrl,
                uploadBaseUrl: self.options.uploadBaseUrl,
                editMode: self.editMode
            });
            fileView.render();
            self.filesUL.append(fileView.el);

        },

        addSingleFile: function (attachedFile) {
            this.filesUL.empty();
            this.addOneFile(attachedFile);
        },

        uploadNewFile: function (file) {
            var self = this;

            this.uploadFileNameP.html(file.name);

            this.gotoUploadingState();

            var newFile = new AttachedFile({
                fullName: this.options.baseName + '/' + file.name,
                shortName: file.name
            });

            this.xhr = new XMLHttpRequest();

            this.xhr.upload.addEventListener('progress', function (evt) {
                if (evt.lengthComputable) {
                    var percentComplete = Math.round(evt.loaded * 100 / evt.total);
                    self.progressBar.width(percentComplete + '%');
                }
            }, false);

            this.xhr.addEventListener('load', function (e) {

                if (e.currentTarget.status !== 200) {
                    alert(e.currentTarget.statusText);
                    self.finished();
                    return false;
                }

                self.finished();
                newFile.isNew = function () {
                    return false;
                };
                self.collection.add(newFile);
                self.newItems.add(newFile);
            }, false);

            var url = this.options.uploadBaseUrl + file.name;
            this.xhr.open('POST', url);

            var fd = new FormData();
            fd.append('upload', file);

            this.xhr.send(fd);
        },

        finished: function () {
            this.gotoIdleState();
        },

        cancelButtonClicked: function () {
            this.xhr.abort();
            this.finished();
        },

        deleteFilesToDelete: function () {
            /*we need to reverse read because model.destroy() remove elements from collection*/
            while (this.filesToDelete.length !== 0) {
                var file = this.filesToDelete.pop();
                file.destroy({
                    error: function () {
                        alert('file ' + file + ' could not be deleted');
                    }
                });
            }
        },

        deleteNewFiles: function () {
            //Abort file upload if there is one
            if (!_.isUndefined(this.xhr)) {
                this.xhr.abort();
            }

            /*deleting unwanted files that have been added by upload*/
            /*we need to reverse read because model.destroy() remove elements from collection*/
            while (this.newItems.length !== 0) {
                var file = this.newItems.pop();
                file.destroy({
                    error: function () {
                        alert('file ' + file + ' could not be deleted');
                    }
                });
            }
        },

        gotoIdleState: function () {
            this.$el.removeClass('uploading');
            this.$el.addClass('idle');
            this.uploadInput.val('');
            this.progressBar.width('0%');
        },

        gotoUploadingState: function () {
            this.$el.removeClass('idle');
            this.$el.addClass('uploading');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, editMode: this.editMode}));

            this.bindDomElements();

            this.addAllFiles();

            return this;
        },

        bindDomElements: function () {
            this.filedroparea = this.$('#filedroparea');
            this.filesUL = this.$('ul.file-list');
            this.uploadFileNameP = this.$('p#upload-file-shortname');
            this.progressBar = this.$('div.bar');
            this.uploadInput = this.$('input#upload-btn');
        }
    });
    return FileListView;
});