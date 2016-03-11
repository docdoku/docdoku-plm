/*global _,define,App,window*/
define([
    'backbone',
    'mustache',
    'unorm',
    'common-objects/views/components/modal',
    'common-objects/models/file/attached_file',
    'common-objects/views/file/file',
    'common-objects/views/alert',
    'text!templates/part/part_import_form.html',
    'common-objects/views/part/import_status_view'
], function (Backbone, Mustache, unorm, ModalView, AttachedFile, FileView, AlertView, template, ImportStatusView) {
    'use strict';
    var PartImportView = Backbone.View.extend({

        template: template,

        tagName: 'div',
        className: 'attachedFiles idle',

        events:{
            'click form button.cancel-upload-btn':'cancelButtonClicked',
            'change form input.upload-btn':'fileSelectHandler',
            'dragover .droppable':'fileDragHover',
            'dragleave .droppable':'fileDragHover',
            'drop .droppable':'fileDropHandler',
            'click .import-button':'formSubmit',
            'click #auto_checkout_part':'changeAutoCheckout',
            'hidden .importer-view':'onHidden'
        },

        initialize: function () {
            this.importStatusViews = [];

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

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));
            this.bindDomElements();
            this.checkboxAutoCheckin.disabled = true;
            this.fetchImports();

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
            this.$('.import-button').removeAttr('disabled');
        },

        bindDomElements: function () {
            this.$modal = this.$('.modal.importer-view');
            this.filedroparea = this.$('.filedroparea');
            this.filedisplay = this.$('#file-selected ul');
            this.uploadInput = this.$('input.upload-btn');
            this.progressBars = this.$('div.progress-bars');
            this.notifications = this.$('div.notifications');
            this.checkboxAutoCheckin = this.$('#auto_checkin_part');
            this.checkboxAutoCheckout = this.$('#auto_checkout_part');
        },

        /**
         * to enable or disable checkbox for auto checkin
         */
        changeAutoCheckout: function () {
            if (this.checkboxAutoCheckout.is(':checked') === true) {
                this.checkboxAutoCheckin.prop('disabled', false);
            } else {
                this.checkboxAutoCheckin.prop('disabled', true);
                this.checkboxAutoCheckin.is('checked', false);
            }
        },
        formSubmit: function () {

            this.clearNotifications();

            var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/import';

            var autocheckin = this.checkboxAutoCheckin.is(':checked');
            var autocheckout = this.checkboxAutoCheckout.is(':checked');
            var permissive = this.$('#permissive_update_part').is(':checked');
            var revisionNote = this.$('#revision_text_part').val().trim();

            if (this.file) {

                var params = {
                    autoCheckout: autocheckout,
                    autoCheckin: autocheckin,
                    permissiveUpdate: permissive,
                    revisionNote: revisionNote
                };

                var importUrl = baseUrl + '?' + $.param(params);

                var xhr = new XMLHttpRequest();
                xhr.onload = this.fetchImports.bind(this);
                xhr.open('POST', importUrl, true);
                var formData = new window.FormData();
                formData.append('upload', this.file);
                xhr.send(formData);

            }else if(!this.file){
                this.printNotifications('error', App.config.i18n.NO_FILE_TO_IMPORT);
            }

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

        printNotifications: function(type,message) {
            this.notifications.append(new AlertView({
                type: type,
                message: message
            }).render().$el);
        },

        clearNotifications: function() {
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
        }

    });
    return PartImportView;

});
