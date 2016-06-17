/*global _,define*/
define([
    'backbone',
    'mustache',
    'models/part_template',
    'text!templates/part-template/part_template_list_item.html',
    'views/part-template/part_template_edit_view',
    'common-objects/utils/date'
], function (Backbone, Mustache, PartTemplate, template, PartTemplateEditView, date) {
    'use strict';
    var PartTemplateListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.reference': 'toPartTemplateEditModal',
            'click .part-attached-files i' : 'openPartTemplateModal'
        },

        tagName: 'tr',

        initialize: function () {
            _.bindAll(this);
            this._isChecked = false;
            this.listenTo(this.model, 'change', this.render);
        },

        render: function () {
            this.$el.html(Mustache.render(template, this.model));
            this.$checkbox = this.$('input[type=checkbox]');
            if (this.isChecked()) {
                this.check();
                this.trigger('selectionChanged', this);
            }
            this.bindUserPopover();
            date.dateHelper(this.$('.date-popover'));
            this.trigger('rendered', this);
            return this;
        },

        selectionChanged: function () {
            this._isChecked = this.$checkbox.prop('checked');
            this.trigger('selectionChanged', this);
        },

        isChecked: function () {
            return this._isChecked;
        },

        check: function () {
            this.$checkbox.prop('checked', true);
            this._isChecked = true;
        },

        unCheck: function () {
            this.$checkbox.prop('checked', false);
            this._isChecked = false;
        },

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), this.model.getId(), 'left');
        },

        toPartTemplateEditModal: function () {
            new PartTemplateEditView({model: this.model});
        },

        openPartTemplateModal: function () {
            var partTemplateEditView = new PartTemplateEditView({model: this.model});
            partTemplateEditView.activateFileTab();
        }

    });

    return PartTemplateListItemView;

});
