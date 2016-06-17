/*global define,App*/
define([
    'common-objects/views/components/modal',
    'text!templates/folder_edit.html'
], function (ModalView, template) {

    'use strict';

    var FolderEditView = ModalView.extend({
        template: template,
        tagName: 'div',
        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['submit #edit-folder-form'] = 'onSubmitForm';
        },
        rendered: function () {
            this.nameInput = this.$('input.name');
            this.nameInput.customValidity(App.config.i18n.REQUIRED_FIELD);
            this.nameInput.val(this.model.get('name'));
            this.previousName = this.model.get('name');
        },

        onSubmitForm: function (e) {

            var name = this.nameInput.val() ? this.nameInput.val().trim():'';

            if (name && name !== this.model.get('name')) {
                this.model.save({
                    name: name
                }, {
                    success: this.success,
                    error: this.error
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },
        success: function (model, response) {
            this.model.id = response.id;
            this.hide();
        },
        error: function (model, error) {
            this.model.set('name',this.previousName);
            if (error.responseText) {
                this.alert({
                    type: 'error',
                    message: error.responseText
                });
            } else {
                console.error(error);
            }
        }
    });
    return FolderEditView;
});
