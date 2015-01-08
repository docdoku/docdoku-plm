/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/part',
    'text!templates/part/part_list_item.html',
    'common-objects/views/share/share_entity',
    'common-objects/views/part/part_modal_view'
], function (Backbone, Mustache, Part, template, ShareView, PartModalView) {
    'use strict';
    var PartListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.part_number': 'toPartModal',
            'click td.part-revision-share i': 'sharePart'
        },

        tagName: 'tr',

        initialize: function () {
            _.bindAll(this);
            this._isChecked = false;
            this.listenTo(this.model, 'change', this.render);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            this.$checkbox = this.$('input[type=checkbox]');
            if (this.isChecked()) {
                this.check();
                this.trigger('selectionChanged', this);
            }
            this.bindUserPopover();
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

        toPartModal: function () {
            var self = this;
            var model = new Part({partKey: self.model.getNumber() + '-' + self.model.getVersion()});
            model.fetch().success(function () {
                new PartModalView({
                    model: model
                }).show();
            });

        },

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), this.model.getNumber(), 'left');
            if (this.model.isCheckout()) {
                this.$('.checkout-user-popover').userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(), 'left');
            }
        },

        sharePart: function () {
            var shareView = new ShareView({model: this.model, entityType: 'parts'});
            window.document.body.appendChild(shareView.render().el);
            shareView.openModal();
        }

    });

    return PartListItemView;

});
