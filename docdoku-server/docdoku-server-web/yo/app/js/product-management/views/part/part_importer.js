/**
 * Created by laurentlevan on 19/02/16.
 */

/*global _,define,App,window*/
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

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click form button.cancel-upload-btn'] = 'cancelButtonClicked';
            this.events['change form input.upload-btn'] = 'fileSelectHandler';
            this.events['dragover .droppable'] = 'fileDragHover';
            this.events['dragleave .droppable'] = 'fileDragHover';
            this.events['drop .droppable'] = 'fileDropHandler';
            this.events['click .import-button'] = 'formSubmit';
            this.events['click #auto_checkout_part'] = 'changeAutoCheckout';

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
            /*var _this = this;*/

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));
            this.bindDomElements();
            this.checkboxAutoCheckin.disabled = true;

            return this;
        },

        loadNewFile: function (file) {

            var fileName = unorm.nfc(file.name);
            var progressBar = $('<div class="progress progress-striped"><div class="bar">' + fileName + '</div></div>');
            //var bar = progressBar.find('.bar');
            this.progressBars.append(progressBar);

            this.gotoUploadingState();

            var newFile = new AttachedFile({
                shortName: fileName
            });

            this.file = file;
            this.addOneFile(newFile);

        },

        addOneFile: function (attachedFile) {
            var self = this;
            self.filedisplay.empty();
            self.filedisplay.append('<li>' + attachedFile.getShortName() + '</li>');
            this.$el.trigger('file:uploaded');
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

        gotoUploadingState: function () {
            //this.$el.removeClass('idle');
            //this.$el.addClass('uploading');
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

            if (this.file != null) {


                var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/import';

                var autocheckin = this.checkboxAutoCheckin.is(':checked');
                var autocheckout = this.checkboxAutoCheckin.is(':checked');
                var permissive = this.$('#permissive_update_part').is(':checked');
                var comment = this.$('#revision_checkbox_part').is(':checked') ? this.$('revision_text_part') : "null";

                var params = {
                    'autoCheckout': autocheckout,
                    'autoCheckin': autocheckin,
                    'permissiveUpdate': permissive,
                    'revisionNote': comment
                };

                var importUrl = baseUrl + '?' + $.param(params);

                var xhr = new XMLHttpRequest();
                xhr.open('PUT', importUrl, true);

                var formdata = new window.FormData();
                formdata.append('upload', this.file);
                debugger;
                console.log(this.file);
                xhr.send(formdata);

            }
            return false;
        },

    });
    return PartImportView;

});
