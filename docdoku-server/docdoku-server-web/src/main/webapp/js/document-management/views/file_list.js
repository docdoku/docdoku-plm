define([
    "i18n!localization/nls/document-management-strings",
    "text!templates/file_list.html",
    "models/attached_file"
], function(i18n, template, AttachedFile) {
    var FileListView = Backbone.View.extend({

        tagName: 'div',
        className: 'attachedFiles idle',

        editMode: true,

        events: {
            "click form button.cancel-upload-btn": "cancelButtonClicked",
            "change form input.upload-btn": "newFileToUpload"
        },

        initialize: function() {
            this.editMode = this.options.editMode;

            this.filesToDelete = new Backbone.Collection();
            this.newItems = new Backbone.Collection();

            this.collection.bind('add', this.addOneFile, this);
        },

        addAllFiles: function() {
            this.collection.each(this.addOneFile, this);
        },

        addOneFile: function(attachedFile) {
            var self = this;

            require(["views/file"], function(FileView) {
                var fileView = new FileView({
                    model: attachedFile,
                    filesToDelete: self.filesToDelete,
                    deleteBaseUrl: self.options.deleteBaseUrl,
                    uploadBaseUrl: self.options.uploadBaseUrl,
                    editMode: self.editMode
                });
                fileView.render();
                self.filesUL.append(fileView.el);
            });
        },

        newFileToUpload: function() {
            var self = this;

            this.gotoUploadingState();

            var shortName = this.uploadInput.val().split(/(\\|\/)/g).pop();

            this.uploadFileNameP.html(shortName);

            var newFile = new AttachedFile({
                shortName: shortName
            });

            this.xhr = new XMLHttpRequest();

            this.xhr.upload.addEventListener("progress", function(evt) {
                if (evt.lengthComputable) {
                    var percentComplete = Math.round(evt.loaded * 100 / evt.total);
                    self.progressBar.width(percentComplete + "%");
                }
            }, false);

            this.xhr.addEventListener("load", function() {
                self.finished();
                self.collection.add(newFile);
                self.newItems.add(newFile);
            }, false);

            var url = this.options.uploadBaseUrl + shortName;
            this.xhr.open("POST", url);

            var file = this.uploadInput[0].files[0];
            var fd = new FormData();
            fd.append("upload", file);

            this.xhr.send(fd);
        },

        finished: function() {
            this.gotoIdleState();
        },

        cancelButtonClicked: function() {
            this.xhr.abort();
            this.finished();
        },

        gotoIdleState: function() {
            this.$el.removeClass("uploading");
            this.$el.addClass("idle");
            this.uploadInput.val("");
        },

        gotoUploadingState: function() {
            this.$el.removeClass("idle");
            this.$el.addClass("uploading");
        },

        render: function() {
            this.$el.html(Mustache.render(template, {i18n: i18n, editMode: this.editMode}));

            this.bindDomElements();

            this.addAllFiles();

            return this;
        },

        bindDomElements: function() {
            this.filesUL = this.$("ul.file-list");
            this.uploadFileNameP = this.$("p#upload-file-shortname");
            this.progressBar = this.$("div.bar");
            this.uploadInput = this.$("input.upload-btn");
        }
    });
    return FileListView;
});