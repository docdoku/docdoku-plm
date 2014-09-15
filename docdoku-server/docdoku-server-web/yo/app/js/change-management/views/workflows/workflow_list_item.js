define([
    "require",
    "common-objects/utils/date",
    "common-objects/views/documents/checkbox_list_item",
    "text!templates/workflows/workflow_list_item.html"
], function (require, Date, CheckboxListItemView, template) {
    var WorkflowListItemView = CheckboxListItemView.extend({

        template: template,

        tagName: "tr",

        initialize: function () {
            CheckboxListItemView.prototype.initialize.apply(this, arguments);
            this.events["click .reference"] = this.actionEdit;
        },

        modelToJSON: function () {
            var data = this.model.toJSON();

            data.creationDate = Date.formatTimestamp(
                APP_CONFIG.i18n._DATE_FORMAT,
                data.creationDate
            );

            return data;
        },

        rendered: function () {
            CheckboxListItemView.prototype.rendered.apply(this, arguments);
            this.$(".author-popover").userPopover(this.model.attributes.author.login, this.model.id, "left");
        },

        actionEdit: function () {
            var url = encodeURI(APP_CONFIG.workspaceId + "/workflow-model-editor/" + this.model.id);
            App.router.navigate(url, {trigger: true});
        }

    });
    return WorkflowListItemView;
});
