/*global _,$,define,App*/
define([
    'backbone',
    'common-objects/views/part/modification_notification_list_item_view',
    'common-objects/models/modification_notification'
], function (Backbone, ModificationNotificationListItemView, ModificationNotification) {
    'use strict';
    var ModificationNotificationListView = Backbone.View.extend({

        render: function () {

            this.modificationNotificationViews = [];

            _.each(this.model.getModificationNotifications(), function (model) {
                    this.addView(new ModificationNotification(model));
                }, this);

            return this;
        },

        addView: function (model) {
            var modificationNotificationView = new ModificationNotificationListItemView({
                model: model
            }).render();

            this.modificationNotificationViews.push(modificationNotificationView);
            this.$el.append(modificationNotificationView.$el);
        }

    });

    return ModificationNotificationListView;
});
