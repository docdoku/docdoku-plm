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
            var data = {
                modificationNotification: this.collection.at(0),
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
        }

    });

    return ModificationNotificationListView;
});
