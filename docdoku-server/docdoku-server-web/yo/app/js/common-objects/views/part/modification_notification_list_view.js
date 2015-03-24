/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/modification_notification_list_item_view',
    'common-objects/models/modification_notification',
    'common-objects/collections/modification_notification_collection',
    'text!common-objects/templates/part/modification_notification_list.html'
], function (Backbone, Mustache, ModificationNotificationListItemView, ModificationNotification, ModificationNotificationCollection, template) {
    'use strict';
    var ModificationNotificationListView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));

            this.modificationNotificationViews = [];

            this.collection.each(this.addView.bind(this));

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
