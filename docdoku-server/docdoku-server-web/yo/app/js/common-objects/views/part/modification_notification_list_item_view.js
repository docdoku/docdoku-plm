/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/modification_notification_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var ModificationNotificationListItemView = Backbone.View.extend({

        events: {
            'click i': 'setAcknowledged'
        },

        render: function () {
            var data = {
                modificationNotification: this.model,
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));

            return this;
        },

        setAcknowledged: function () {
            var _this = this;
            this.model.setAcknowledged(function () {
                _this.isAcknowledged();
            });
        },

        isAcknowledged: function () {
            this.model.set('acknowledged', true);
            this.render();
        }

    });

    return ModificationNotificationListItemView;
});
