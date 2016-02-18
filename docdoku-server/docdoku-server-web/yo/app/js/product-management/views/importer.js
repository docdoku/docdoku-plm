/**
 * Created by laurent on 16/02/16.
 */

define([
    'backbone',
    'mustache',
    'text!templates/importer.html',
    'common-objects/views/alert',
    'common-objects/views/prompt'
], function (Backbone, Mustache, template, AlertView, PromptView) {
    'use strict';
    var ImporterView = Backbone.View.extend({

        events : {
            'click .import-button':'onImport',
            'dragover .droppable': 'fileDragHover',
            'dragleave .droppable': 'fileDragHover',
            'drop .droppable': 'fileDropHandler'
        },

        delimiter:',',


        initialize:function(){

        },

        onImport:function(){
            var self = this;

            var promptView = new PromptView();
            //TODO: add message
            window.document.body.appendChild(promptView.render().el);
            promptView.openModal();
        },

        fileDropHandler: function (e) {
            this.fileDragHover(e);
            if (this.options.singleFile && e.dataTransfer.files.length > 1) {
                this.printNotifications('error', App.config.i18n.SINGLE_FILE_RESTRICTION);
                return;
            }

            _.each(e.dataTransfer.files, this.uploadNewFile.bind(this));
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


        fileSelectHandler: function (e) {
            _.each(e.target.files, this.uploadNewFile.bind(this));
        },



    });

    return ImporterView;

});
