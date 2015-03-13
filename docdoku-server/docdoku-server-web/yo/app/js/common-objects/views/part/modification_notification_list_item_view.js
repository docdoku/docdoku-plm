/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/modification_notification_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var ModificationNotificationListItemView = Backbone.View.extend({

        events: {
        },

        initialize: function () {
        },

        render: function () {
            var data = {
                modificationNotification: this.model,
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));

            return this;
        }

    });

    return ModificationNotificationListItemView;
});
