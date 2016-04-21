/*global _,define,App*/
define([
    'backbone',
    'common-objects/models/modification_notification'
], function (Backbone, ModificationNotification) {
    'use strict';
    var ModificationNotificationCollection = Backbone.Collection.extend({

        model: ModificationNotification,
        className: 'ModificationNotificationCollection',

        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/notifications/';
        },

        hasUnreadModificationNotifications: function () {
            return _.select(this.models || [], function(notif) {
                return !notif.isAcknowledged();
            }).length;
        }

    });

    return ModificationNotificationCollection;
});
