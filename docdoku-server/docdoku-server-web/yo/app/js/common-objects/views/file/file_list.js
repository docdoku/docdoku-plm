/*global _,$,define,App,window*/
define([
    'backbone',
    'mustache',
    'unorm',
    'text!common-objects/templates/file/file_list.html',
    'common-objects/models/file/attached_file',
    'common-objects/views/file/file',
    'common-objects/views/alert'
], function (Backbone, Mustache, unorm, template, AttachedFile, FileView, AlertView) {
    'use strict';
	var FileListView = Backbone.View.extend({

        tagName: 'div',
        className: 'attachedFiles idle',

        editMode: true,

        events: {
            'click form button.cancel-upload-btn': 'cancelButtonClicked',
            'change form input.upload-btn': 'fileSelectHandler',
            'dragover .droppable': 'fileDragHover',
            'dragleave .droppable': 'fileDragHover',
            'drop .droppable': 'fileDropHandler',
            'submit form':'formSubmit'
        },

        initialize: function () {
            this.editMode = this.options.editMode;
            this.title = this.options.title;

            this.xhrs = [];

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
            if(this.options.singleFile && e.dataTransfer.files.length > 1){
                window.alert(App.config.i18n.SINGLE_FILE_RESTRICTION);
                return;
            }

            _.each(e.dataTransfer.files, this.uploadNewFile.bind(this));
        },

        fileSelectHandler: function (e) {
            _.each(e.target.files, this.uploadNewFile.bind(this));
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
            this.$el.trigger('file:uploaded');
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

            var xhr = new XMLHttpRequest();

            xhr.upload.addEventListener('progress', function (evt) {
                if (evt.lengthComputable) {
                    var percentComplete = Math.round(evt.loaded * 100 / evt.total);
                    bar.width(percentComplete + '%');
                }
            }, false);

            xhr.addEventListener('load', function (e) {

                if (e.currentTarget.status !== 200 && e.currentTarget.status !== 201) {
                    window.alert(e.currentTarget.statusText);
                    self.xhrFinished(xhr);
                    progressBar.remove();
                    return false;
                }

                self.xhrFinished(xhr);
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

            xhr.open('POST', this.options.uploadBaseUrl);

            var fd = new window.FormData();
            fd.append('upload', file);

            xhr.send(fd);

            this.xhrs.push(xhr);
        },

        xhrFinished : function(xhr) {
            this.xhrs.splice(this.xhrs.indexOf(xhr), 1);
            if (!this.xhrs.length) {
                this.gotoIdleState();

                this.notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.FILE_UPLOADED
                }).render().$el);
            }
        },

        finished: function () {
            this.gotoIdleState();
        },

        cancelButtonClicked: function () {
            _.invoke(this.xhrs,'abort');
            this.finished();
        },

        deleteFilesToDelete: function () {
            /*we need to reverse read because model.destroy() remove elements from collection*/
            while (this.filesToDelete.length !== 0) {
                var file = this.filesToDelete.pop();
	            this.deleteAFile(file);
            }
        },

        deleteNewFiles: function () {
            //Abort file upload if there is one
            _.invoke(this.xhrs,'abort');

            //deleting unwanted files that have been added by upload
            //we need to reverse read because model.destroy() remove elements from collection
            while (this.newItems.length !== 0) {
                var file = this.newItems.pop();
	            this.deleteAFile(file);
            }
        },

		deleteAFile: function(file){
			file.destroy({
				dataType: 'text', // server doesn't send a json hash in the response body
				error: function () {
					window.alert(App.config.i18n.FILE_DELETION_ERROR.replace('%{id}',file.id));
				}
			});
		},

        gotoIdleState: function () {
            this.$el.removeClass('uploading');
            this.$el.addClass('idle');
            this.uploadInput.val('');
        },

        gotoUploadingState: function () {
            this.$el.removeClass('idle');
            this.$el.addClass('uploading');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, title:this.title, editMode: this.editMode, multiple:!this.options.singleFile}));

            this.bindDomElements();

            this.addAllFiles();

            return this;
        },

        formSubmit:function(e){

            return false;
        },

        bindDomElements: function () {
            this.filedroparea = this.$('.filedroparea');
            this.filesUL = this.$('ul.file-list');
            this.uploadInput = this.$('input#upload-btn');
            this.progressBars = this.$('div.progress-bars');
            this.notifications = this.$('div.notifications');
        }
    });
    return FileListView;
});
