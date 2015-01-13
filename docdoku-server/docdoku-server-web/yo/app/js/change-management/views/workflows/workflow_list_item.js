/*global define,App*/
define([
    'require',
    'common-objects/utils/date',
    'common-objects/views/documents/checkbox_list_item',
    'text!templates/workflows/workflow_list_item.html'
], function (require, date, CheckboxListItemView, template) {
	'use strict';
    var WorkflowListItemView = CheckboxListItemView.extend({

        template: template,

        tagName: 'tr',

        initialize: function () {
            CheckboxListItemView.prototype.initialize.apply(this, arguments);
            this.events['click .reference'] = this.actionEdit;
        },

        modelToJSON: function () {
            var data = this.model.toJSON();

            data.creationDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                data.creationDate
            );

            return data;
        },

        rendered: function () {
            CheckboxListItemView.prototype.rendered.apply(this, arguments);
            this.$('.author-popover').userPopover(this.model.attributes.author.login, this.model.id, 'left');
            date.dateHelper(this.$('.date-popover'));
        },

        actionEdit: function () {
            var url = encodeURI(App.config.workspaceId + '/workflow-model-editor/' + this.model.id);
            App.router.navigate(url, {trigger: true});
        }

    });
    return WorkflowListItemView;
});
