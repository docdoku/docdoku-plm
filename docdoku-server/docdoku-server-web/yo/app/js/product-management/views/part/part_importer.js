/**
 * Created by laurentlevan on 19/02/16.
 */

/*global _,define,App,window,confirm*/
define([
    'backbone',
    'mustache',
    'unorm',
    'common-objects/views/components/modal',
    'common-objects/models/file/attached_file',
    'common-objects/views/file/file',
    'common-objects/views/alert',
    'text!templates/part/part_import_form.html'
], function (Backbone, Mustache, unorm, ModalView, AttachedFile, FileView, AlertView, template) {
    'use strict';
    var PartImportView = ModalView.extend({

        template: template,

        tagName: 'div',
        className: 'attachedFiles idle',

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click form button.cancel-upload-btn'] = 'cancelButtonClicked';
            this.events['change form input.upload-btn'] = 'fileSelectHandler';
            this.events['dragover .droppable'] = 'fileDragHover';
            this.events['dragleave .droppable'] = 'fileDragHover';
            this.events['drop .droppable'] = 'fileDropHandler';
            this.events['click .import-button'] = 'formSubmit';
            this.events['click #auto_checkout_part'] = 'changeAutoCheckout';

            // Prevent browser behavior on file drop
            window.addEventListener('drop', function (e) {
                e.preventDefault();
                return false;
            }, false);

            window.addEventListener('ondragenter', function (e) {
                e.preventDefault();
                return false;
            }, false);

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
        },

        bindDomElements: function () {
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
            var revisionNote = this.$('#revision_note_checkbox_part').is(':checked') ? this.$('#revision_text_part').val()  : '';

            var emptyRevision =false;

            if(revisionNote && !this.$('#revision_text_part').val()){
                this.printNotifications('error',App.config.i18n.EMPTY_REVISION_NOTE);
                emptyRevision = true;
            }

            if (this.file && !emptyRevision) {



                var params = {
                    'autoCheckout': autocheckout,
                    'autoCheckin': autocheckin,
                    'permissiveUpdate': permissive,
                    'revisionNote': revisionNote
                };

                var importUrl = baseUrl + '?' + $.param(params);

                var xhr = new XMLHttpRequest();
                xhr.open('POST', importUrl, true);

                if(confirm(App.config.i18n.CONFIRM_IMPORT)){
                    var formdata = new window.FormData();
                    formdata.append('upload', this.file);
                    xhr.send(formdata);

                    this.$('#import_pending').html('<i class="fa fa-refresh fa-spin" title="'+App.config.i18n.IMPORT_PENDING+'"></i><b> '+App.config.i18n.IMPORT_PENDING+' ...</b>');
                }

            }else if(!this.file){

                this.printNotifications('error', App.config.i18n.NO_FILE_TO_IMPORT);
            }


            return false;
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

    });
    return PartImportView;

});
