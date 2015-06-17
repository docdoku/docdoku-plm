/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'async',
    'common-objects/views/part/modification_notification_list_item_view',
    'common-objects/models/modification_notification',
    'common-objects/collections/modification_notification_collection',
    'text!common-objects/templates/part/modification_notification_list.html'
], function (Backbone, Mustache, Async, ModificationNotificationListItemView, ModificationNotification, ModificationNotificationCollection, template) {
    'use strict';
    var ModificationNotificationListView = Backbone.View.extend({

        events: {
            'click .action-group-acknowledge': 'acknowledgeAll'
        },

        initialize: function () {
            _.bindAll(this);
            this.events['notification:acknowledged'] = 'render';
        },

        render: function () {
            var data = {
                modificationNotification: this.collection.at(0),
                hasUnreadModificationNotifications: this.collection.hasUnreadModificationNotifications() > 1,
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();

            this.modificationNotificationViews = [];

            this.collection.each(this.addView.bind(this));

            return this;
        },

        bindDomElements: function () {
            this.$items = this.$('.items');
        },

        addView: function (model) {
            var modificationNotificationView = new ModificationNotificationListItemView({
                model: model
            }).render();

            this.modificationNotificationViews.push(modificationNotificationView);
            this.$items.append(modificationNotificationView.$el);
        },

        acknowledgeAll: function () {
            var _this = this;
            var data = {ackComment: this.geGroupAcknowledgementComment()};

            Async.each(this.collection.models, function(notif, callback) {
                if (notif.isAcknowledged()) {
                    callback();
                } else {
                    notif.setAcknowledged(data).success(callback);
                }

            }, function(err) {
                if (!err) {
                    _this.$el.trigger('notification:acknowledged');
                }
            });
        },

        geGroupAcknowledgementComment: function () {
            return this.$('#group-acknowledgement-comment').val() || null;
        }

    });

    return ModificationNotificationListView;
});
