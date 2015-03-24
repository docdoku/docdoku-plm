/*global _,define*/
define([
    'backbone',
    'common-objects/models/modification_notification'
], function (Backbone, ModificationNotification) {
    'use strict';
    var ModificationNotificationCollection = Backbone.Collection.extend({

        model: ModificationNotification,
        className: 'ModificationNotificationCollection',

        setUrl: function (url) {
            this.url = url;
        }

    });

    return ModificationNotificationCollection;
});
