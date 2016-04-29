/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/bom_item.html',
    'common-objects/views/part/part_modal_view',
    'common-objects/views/share/share_entity',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, PartModalView, ShareView, date) {
    'use strict';
    var BomItemView = Backbone.View.extend({

        tagName: 'tr',

        events: {
            'click td.modification_notification i': 'toPartModalOnNotificationsTab',
            'click .part_number': 'onPartClicked',
            'click td.part-revision-share i': 'sharePart',
            'click td.part-attached-files i': 'toPartModalOnFilesTab'
        },

        initialize: function () {
            this.listenTo(this.model, 'sync, reset, change', this.render);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            this.$input = this.$('input');
            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), this.model.getName(), 'left');
            if (this.model.isCheckout()) {
                this.$('.checkout-user-popover').userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(), 'left');
            }
            date.dateHelper(this.$('.date-popover'));
            return this;
        },

        onPartClicked: function () {
            var self = this;
            self.model.fetch().success(function () {
                new PartModalView({
                    model: self.model
                });
            });
        },

        toPartModalOnNotificationsTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var partModalView = new PartModalView({
                    model: model
                });
                partModalView.show();
                partModalView.activateNotificationsTab();
            });
        },

        toPartModalOnFilesTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var partModalView = new PartModalView({
                    model: model
                });
                partModalView.show();
                partModalView.activateFileTab();
            });
        },

        sharePart: function () {
            var shareView = new ShareView({model: this.model, entityType: 'parts'});
            window.document.body.appendChild(shareView.render().el);
            shareView.openModal();
        },

        isChecked: function () {
            return this.$input[0].checked;
        },

        setSelectionState: function (state) {
            this.$input[0].checked = state;
        }

    });

    return BomItemView;

});
