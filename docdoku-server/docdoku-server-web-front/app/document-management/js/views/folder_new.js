/*global define,App*/
define([
    'common-objects/views/components/modal',
    'text!templates/folder_new.html'
], function (ModalView, template) {

    'use strict';

    var FolderNewView = ModalView.extend({
        template: template,
        tagName: 'div',
        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['submit #new-folder-form'] = 'onSubmitForm';
        },
        rendered:function(){
            this.nameInput = this.$('input.name');
            this.nameInput.customValidity(App.config.i18n.REQUIRED_FIELD);
        },

        onSubmitForm: function (e) {

            var name = this.nameInput.val() ? this.nameInput.val().trim():'';

            if (name) {
                this.collection.create({
                    name: name
                }, {
                    url: this.collection.url(),
                    success: this.success,
                    error: this.error
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        success: function () {
            this.hide();
            this.parentView.show();
        },
        error: function (model, error) {
            if (error.responseText) {
                this.alert({
                    type: 'error',
                    message: error.responseText
                });
                this.collection.remove([model]);
            } else {
                console.error(error);
            }
            this.collection.remove([model]);
        }
    });
    return FolderNewView;
});
