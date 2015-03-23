/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/modification_notification_list_item_view',
    'common-objects/models/modification_notification',
    'text!common-objects/templates/part/modification_notification_list.html'
], function (Backbone, Mustache, ModificationNotificationListItemView, ModificationNotification, template) {
    'use strict';
    var ModificationNotificationListView = Backbone.View.extend({

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));

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
