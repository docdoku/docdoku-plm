/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/modification_notification_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var ModificationNotificationListItemView = Backbone.View.extend({

        events: {
            'click .action-acknowledge': 'acknowledge'
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.model, 'change', this.render);
        },

        render: function () {
            var data = {
                modificationNotification: this.model,
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindUserPopover();
            return this;
        },

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthor().login, this.model.getImpactedPartNumber(), 'right');
            if (this.model.isAcknowledged() && this.model.getAckAuthor()) {
                this.$('.ack-author-popover').userPopover(this.model.getAckAuthor().login, this.model.getImpactedPartNumber(), 'right');
            }
        },

        acknowledge: function () {
            var _this = this;
            var data = {ackComment: this.getAcknowledgementComment()};
            this.model.setAcknowledged(data).success(function () {
                _this.$el.trigger('notification:acknowledged');
            });
        },

        getAcknowledgementComment: function () {
            return this.$('#acknowledgement-comment').val() || null;
        }

    });

    return ModificationNotificationListItemView;
});
