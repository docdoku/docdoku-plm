/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/modification_notification_list_view',
    'common-objects/models/modification_notification',
    'common-objects/collections/modification_notification_collection',
    'text!common-objects/templates/part/modification_notification_group_list.html'
], function (Backbone, Mustache, ModificationNotificationListView, ModificationNotification, ModificationNotificationCollection, template) {
    'use strict';
    var ModificationNotificationGroupListView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));

            this.modificationNotificationViews = [];

            this.groupedMap = _.groupBy(
                this.collection.models,
                function (notif) {
                    return notif.getModifiedPartNumber();
                },
                this
            );

            _.each(_.keys(this.groupedMap), this.addListView, this);

            return this;
        },

        addListView: function (key) {
            var modificationNotificationView = new ModificationNotificationListView({
                collection: new ModificationNotificationCollection(this.groupedMap[key])
            }).render();

            this.modificationNotificationViews.push(modificationNotificationView);
            this.$el.append(modificationNotificationView.$el);
        }

    });

    return ModificationNotificationGroupListView;
});
