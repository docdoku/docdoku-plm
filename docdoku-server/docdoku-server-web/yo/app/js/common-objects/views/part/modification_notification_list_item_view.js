/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/modification_notification_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var ModificationNotificationListItemView = Backbone.View.extend({

        events: {
            'click i': 'acknowledge'
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.model, 'change:acknowledged', this.render);
        },

        render: function () {
            var data = {
                modificationNotification: this.model,
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));

            return this;
        },

        acknowledge: function () {
            this.model.setAcknowledged();
        }

    });

    return ModificationNotificationListItemView;
});
