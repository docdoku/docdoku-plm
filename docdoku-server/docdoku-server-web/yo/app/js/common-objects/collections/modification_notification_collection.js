/*global define,App*/
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
        }

    });

    return ModificationNotificationCollection;
});
