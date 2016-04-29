/*global define,App,_*/
define([
    'common-objects/utils/date',
    'common-objects/views/documents/checkbox_list_item',
    'views/template_edit',
    'text!templates/template_list_item.html'
], function (date, CheckboxListItemView, TemplateEditView, template) {

    'use strict';

    var TemplateListItemView = CheckboxListItemView.extend({

        template: template,
        tagName: 'tr',
        initialize: function () {
            CheckboxListItemView.prototype.initialize.apply(this, arguments);
            this.events['click .reference'] = this.actionEdit;
            this.events['click .document-attached-files i'] = this.openDocumentTemplateModal;
        },

        rendered: function () {
            CheckboxListItemView.prototype.rendered.apply(this, arguments);
            this.$('.author-popover').userPopover(this.model.attributes.author.login, this.model.id, 'left');
            date.dateHelper(this.$('.date-popover'));
        },

        modelToJSON: function () {
            var data = this.model.toJSON();
            // Format dates
            if (!_.isUndefined(data.creationDate)) {
                data.creationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.creationDate);
            }
            if (!_.isUndefined(data.modificationDate)) {
                data.modificationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.modificationDate);
            }
            if (this.model.hasACLForCurrentUser()) {
                data.isReadOnly = this.model.isReadOnly();
                data.isFullAccess = this.model.isFullAccess();
            }
            data.hasAttachedFiles = this.model.getAttachedFiles().length;
            return data;
        },
        actionEdit: function () {
            var that = this;
            this.model.fetch().success(function () {
                that.editView = that.addSubView(
                    new TemplateEditView({
                        model: that.model
                    })
                );
                window.document.body.appendChild(that.editView.el);
            });
        },

        openDocumentTemplateModal: function () {
            var that = this;
            this.model.fetch().success(function () {
                that.editView = that.addSubView(
                    new TemplateEditView({
                        model: that.model
                    })
                );
                that.editView.activateFileTab();
                window.document.body.appendChild(that.editView.el);



            });
        }
    });
    return TemplateListItemView;
});
